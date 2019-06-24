package io.yooksi.commons.git;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;
import io.yooksi.commons.bash.UnixPath;
import io.yooksi.commons.define.MethodsNotNull;
import io.yooksi.commons.logger.LibraryLogger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.util.FileUtils;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.Positive;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@MethodsNotNull
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class Git extends org.eclipse.jgit.api.Git {

    private final java.util.Map<RevCommit, String> stashMap =
            java.util.Collections.synchronizedMap(new java.util.Hashtable<>());
    
    private final UnixPath repoRootDirPath;

    /**
     * Construct a new {@link Git} object which can interact with the specified git repository.
     * <p>
     * All command classes returned by methods of this class will always
     * interact with this git repository.
     * <p>
     * The caller is responsible for closing the repository; {@link #close()} on
     * this instance does not close the repo.
     *
     * @param repo the git repository this class is interacting with;
     *             {@code null} is not allowed.
     */
    public Git(Repository repo) {
        super(repo);
        repoRootDirPath = UnixPath.get(repo.getDirectory().getParentFile());
    }

    /**
     * Open Git repository located in root directory.
     *
     * @return a {@link org.eclipse.jgit.api.Git} object for the git repository in root directory
     * @throws IllegalStateException if the git repository was not found
     *
     * @see org.eclipse.jgit.api.Git#open(File)
     */
    public static Git openRepository() {

        try {
            LibraryLogger.debug("Opening Git repository in root directory.");
            return new Git(open(new File(".git")).getRepository());
        }
        catch (IOException e) {
            throw new IllegalStateException("Unable to open Git repository in root directory", e);
        }
    }

    /**
     * Open Git repository located under given path.
     *
     * @param repoPath {@code Path} to the repository to open
     * @return a {@link org.eclipse.jgit.api.Git} object for the existing git repository
     *
     * @throws java.io.FileNotFoundException if the file represented by the given path does not exist.
     * @throws IOException if the repository could not be accessed to configure builder's parameters.
     *
     * @see org.eclipse.jgit.api.Git#open(File)
     */
    public static Git openRepository(Path repoPath) throws IOException {

        File repo = repoPath.toFile();
        if (!repo.exists()) {
            String log = "Unable to find Git repository under path \"%s\"";
            throw new java.io.FileNotFoundException(String.format(log, repoPath.toAbsolutePath().toString()));
        }
        else {
            LibraryLogger.debug("Opening Git repository under path " + repoPath.toAbsolutePath().toString());
            return new Git(org.eclipse.jgit.api.Git.open(repo).getRepository());
        }
    }

    /**
     * <p>Create an empty git repository or reinitialize an existing one.</p>
     * <p>
     *     This command creates an empty Git repository - basically a {@code .git} directory
     *     with subdirectories for {@code objects, refs/heads, refs/tags}, and template files.
     *     An initial {@code HEAD} file that references the {@code HEAD}
     *     of the master branch is also created.
     * </p>
     *     Running <i>git init</i> in an existing repository is safe.
     *     It will not overwrite things that are already there.
     *     The primary reason for rerunning <i>git init</i> is to pick up newly added templates
     *     (or to move the repository to another place if --separate-git-dir is given).
     * </p>
     * @param rootDirPath {@code Path} to the directory we want to initialize our repository in.
     *                    Note that path can <i>(but should not)</i> point to the repository meta
     *                    directory ({@code .git}) in which case a warning will be logged.
     *
     * @return an instance of the (re)initialized repository
     * @throws GitAPIException if an exception occurred while executing {@link InitCommand#call()}.
     *
     * @see org.eclipse.jgit.api.Git#init()
     */
    public static Git initRepository(Path rootDirPath) throws IOException, GitAPIException {

        File rootDir = rootDirPath.toFile();
        InitCommand command;

        if (!rootDir.getName().equals(".git")) {
            command = Git.init().setDirectory(rootDir);
        }
        else {
            LibraryLogger.warn("Tried to initialize a Git repository inside a recursive directory (.git\\.git).");
            command = Git.init().setGitDir(rootDir);
        }
        if (!rootDir.exists()) {
            FileUtils.mkdirs(rootDir);
        }
        String absPath = rootDirPath.toAbsolutePath().toString();
        LibraryLogger.debug("Initializing Git repository in directory " + absPath);
        return new Git(command.call().getRepository());
    }

    /**
     * @return {@code String} form of the commit's SHA-1, in lower case hexadecimal.
     */
    public static String getCommitSHA(RevCommit commit) {
        return commit.toObjectId().getName();
    }

    /**
     * Put the stash object in internal storage.
     *
     * @param stash reference to the stashed commit
     * @return reference to the stashed commit
     *
     * @throws NullPointerException if the given {@code RevCommit} object is null
     */
    private synchronized RevCommit registerStash(RevCommit stash, String branch) {

        stashMap.put(stash, branch);
        return stash;
    }
    /**
     * Remove the stash object from internal storage.
     * @param stash reference to the stashed commit
     */
    private synchronized void unregisterStash(RevCommit stash) {

        if (stashMap.remove(stash) == null) {
            LibraryLogger.warn("Tried to remove unregistered stash " + stash);
        }
    }

    /**
     * Create a new commit containing the current contents of the index and the given log message
     * describing the changes. The new commit is a direct child of {@code HEAD}, usually the tip
     * of the current branch, and the branch is updated to point to it (unless no branch is
     * associated with the working tree, in which case {@code HEAD} is "detached" as described
     * in <a href=https://git-scm.com/docs/git-checkout>git-checkout[1]</a>)
     *
     * @return A reference to the commit
     * @throws GitAPIException if an exception occurred while executing {@link CommitCommand#call()}.
     *
     * @see org.eclipse.jgit.api.Git#commit()
     */
    public RevCommit commit(String message) throws GitAPIException {

        LibraryLogger.debug("Committing all indexed files");
        return commit().setMessage(message).call();
    }

    /**
     * Add a path to a {@code file/directory} whose content should be added.
     * A directory name (e.g. dir to add dir/file1 and dir/file2) can
     * also be given to add all files in the directory, recursively.
     *
     * @param path {@code Path} to the file to add
     * @return reference to the index file just added
     * @throws GitAPIException if an exception occurred while executing {@link AddCommand#call()}.
     *
     * @see org.eclipse.jgit.api.Git#add()
     * @see AddCommand#addFilepattern(String)
     */
    public DirCache add(Path path) throws GitAPIException {

        LibraryLogger.debug("Adding \"%s\" to indexed files.", path.toString());
        return add().addFilepattern(relativizePath(path).toString()).call();
    }

    /**
     * Updates files in the working tree to match the version in the index or the specified tree.
     * If no paths are given, git checkout will also update {@code HEAD} to set the
     * specified branch as the current branch.
     *
     * @param branch name of the branch to checkout
     * @param create whether to create the branch if it does not already exist
     * @return a reference to the checked out branch
     *
     * @throws IOException if the reference space under given branch cannot be accessed.
     *                     This exception is thrown from {@link RefDatabase#exactRef(String)}.
     *                     The exact reason is not well documented.
     *
     * @throws GitAPIException if an exception occurred while executing {@link CheckoutCommand#call()}.
     * @throws IllegalStateException if the checkout operation failed due to unresolved conflicts.
     *
     * @see org.eclipse.jgit.api.Git#checkout()
     * @see CheckoutCommand#setCreateBranch(boolean)
     */
    public Ref checkoutBranch(String branch, boolean create) throws IOException, GitAPIException {

        try {
            CheckoutCommand cmd = checkout().setName(branch);
            if (create && getRepository().findRef(branch) == null)
            {
                LibraryLogger.debug("Create and checkout new branch " + branch);
                //noinspection ConstantConditions
                cmd.setCreateBranch(create);
            }
            else LibraryLogger.debug("Checking out branch " + branch);
            return cmd.call();
        }
        /* This exception will be thrown when we cant checkout because of unresolved conflicts.
         * So we're gonna stash the changes and try to checkout the branch again.
         */
        catch (CheckoutConflictException e1) {

            try {
                stashChanges();
                return checkoutBranch(branch, create);
            }
            catch (CheckoutConflictException e2)
            {
                String log = "Unable to checkout branch %s due to unresolved conflicts.";
                throw new IllegalStateException(String.format(log, branch), e2);
            }
        }
    }

    /**
     * Retrieve last commit reference with {@link LogCommand} then parse
     * and decode the complete commit message to a string.
     *
     * @return last commit's full message
     *
     * @throws NoHeadException if no HEAD exists and no explicit starting revision was specified.
     * @throws GitAPIException if an exception occurred while executing {@link LogCommand#call()}.
     *
     * @see org.eclipse.jgit.api.Git#log()
     * @see RevCommit#getFullMessage()
     */
    public String getLastCommitMessage() throws GitAPIException {

        java.util.Iterator<RevCommit> commits = log().call().iterator();
        return commits.hasNext() ? commits.next().getFullMessage() : "";
    }

    /**
     * Compile and return a {@code List} of all commits found on the current branch.
     * The order of list elements will be the same as the order at which they were originally compiled
     * by {@link LogCommand#all()}. This insertion sorting order should naturally correlate to values
     * provided by {@link RevCommit#commitTime} from newest to oldest. In short the newest commit will
     * be the first element, and the oldest commit will be the last element of the returned list.
     *
     * @return {@code List} of all commits found on the current branch.
     * @throws IOException if some commit references could not be accessed
     * @throws GitAPIException if an exception occurred while executing {@link LogCommand#call()}.
     *
     * @see org.eclipse.jgit.api.Git#log()
     */
    public List<RevCommit> getAllCommits() throws IOException, GitAPIException {

        List<RevCommit> commits = new ArrayList<>();
        log().all().call().forEach(commits::add);
        return commits;
    }

    /**
     * @return a reference on the current branch that corresponds to {@code HEAD}.
     * @throws IOException if the reference space cannot be accessed.
     */
    public Ref getHead() throws IOException {
        return getRepository().findRef(Constants.HEAD);
    }

    /**
     * Rewind the current {@code HEAD} for {@code N} steps using the specified mode.
     * This operation is equivalent to executing {@code git reset HEAD~N} with {@code N}
     * being the number of commits to reset. Internally the method will retrieve a list of
     * all commits currently residing on the current branch and perform the {@code reset}
     * command with the {@code ref} set to the {@code SHA} of the {@code Nth} commit.
     *
     * @param steps number of commits (from {@code HEAD}) to rewind.
     * @param mode {@code ResetType} to use when configuring the reset command.
     * @return reference to the new {@code HEAD} of the current branch.
     *
     * @throws IOException thrown by {@link #getAllCommits()} if some commit references could not be accessed.
     * @throws GitAPIException if an exception occurred while executing {@link ResetCommand#call()}.
     *
     * @see org.eclipse.jgit.api.Git#reset()
     */
    public Ref rewind(@Positive int steps, @Nullable ResetCommand.ResetType mode) throws IOException, GitAPIException {

        if (steps < 0) {
            String log = "Cannot reset current HEAD for %d steps. Value must be a positive number.";
            throw new IllegalArgumentException(String.format(log, steps));
        }
        List<RevCommit> commits = getAllCommits();
        if (steps >= commits.size())
        {
            String log = "Cannot reset current HEAD for %d steps. Not enough commits (%d) to accommodate the request.";
            throw new IndexOutOfBoundsException(String.format(log, steps, commits.size()));
        }
        String ref = commits.get(steps).getName();
        LibraryLogger.debug("Performing %s reset on HEAD to ref \"%s\"", mode, ref);
        return reset().setRef(ref).setMode(mode != null ? mode : ResetCommand.ResetType.MIXED).call();
    }

    /**
     * Use this method when you want to record the current state of the working directory and the index,
     * but want to go back to a clean working directory. The command saves your local modifications away
     * and reverts the working directory to match the {@code HEAD} commit.
     *
     * @return reference to the stashed commit
     *
     * @throws GitAPIException if an exception occurred while executing {@link StashCreateCommand#call()}
     * @throws IOException when we're unable to resolve current branch
     *
     * @see org.eclipse.jgit.api.Git#stashCreate()
     */
    public RevCommit stashChanges() throws GitAPIException, IOException {

        String branch = getRepository().getBranch();
        LibraryLogger.debug("Stashing local changes on branch " + branch);

        return registerStash(stashCreate().call(), branch);
    }

    /**
     * Apply the changes in the last stashed commit to the working directory and index.
     * This operation is similar to {@code pop}, but does not remove the state from the
     * stash list. Unlike {@code pop, <stash>} may be any commit that looks like a commit
     * created by {@code stash push} or {@code stash create}.
     *
     * @throws GitAPIException if an exception occurred while executing
     * {@link StashApplyCommand#call()} or {@link StashListCommand#call()}.
     *
     * @throws IOException when we're unable to resolve current branch
     *
     * @see org.eclipse.jgit.api.Git#stashApply()
     */
    public void applyStash() throws GitAPIException, IOException {

        String branch = getRepository().getBranch();
        LibraryLogger.debug("Applying stashed changes on branch " + branch);

        java.util.Collection<RevCommit> stashes = stashList().call();

        stashApply().call();
    }

    public String[] diff(@Nullable TreeFilter filter) throws GitAPIException, IOException {

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            diff().setPathFilter(filter == null ? TreeFilter.ALL : filter).setOutputStream(stream).call();
            return stream.toString(Charset.defaultCharset()).split(LineSeparator.Unix);
        }
    }

    public List<DiffEntry> diff(AbstractTreeIterator from, AbstractTreeIterator to, java.io.OutputStream out,
                                @Nullable TreeFilter filter) throws GitAPIException {

            return diff().setOldTree(from).setNewTree(to)
                    .setPathFilter(filter == null ? TreeFilter.ALL : filter).setOutputStream(out).call();
    }

    /**
     * Construct and return a path relative to repository root directory path.
     * The given path can be both {@code Unix} or {@code Windows} compatible.
     *
     * @param path {@code Path} to relativize
     * @return {@code UnixPath} that represents a relative path between repository
     *         root directory path and the given path.
     *
     * @see FileUtils#relativizeGitPath(String, String)
     */
    public UnixPath relativizePath(Path path) {
        return UnixPath.get(FileUtils.relativizeGitPath(repoRootDirPath.toString(), UnixPath.convert(path)));
    }
}

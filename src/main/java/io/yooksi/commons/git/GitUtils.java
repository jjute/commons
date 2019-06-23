package io.yooksi.commons.git;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.StashCreateCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class GitUtils {

    private static final Git REPO = openRepository();
    private static boolean stashedChanges;

    private static Git openRepository() {
        try {
            return Git.open(new java.io.File(".git"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static RevCommit commit(String message) {

        System.out.println("Commit indexed files");
        try {
            return REPO.commit().setMessage(message).call();
        }
        catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean hasStashedChanges() {
        return stashedChanges;
    }

    public static void add(java.nio.file.Path path) {

        try {
            String sPath = path.toString().replace("\\", "/");
            REPO.add().addFilepattern(sPath).call();
        }
        catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Ref checkoutBranch(String branch, boolean create) {

        try {
            CheckoutCommand cmd = REPO.checkout().setName(branch);
            if (create && REPO.getRepository().findRef(branch) == null)
            {
                System.out.println("Create and checkout new branch " + branch);
                //noinspection ConstantConditions
                cmd.setCreateBranch(create);
            }
            else System.out.println("Checkout branch " + branch);
            return cmd.call();
        }
        catch (CheckoutConflictException e) {
            stashChanges();
            return checkoutBranch(branch, create);
        }
        catch (GitAPIException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static RevCommit stashChanges() {

        System.out.println("Stash changes");
        try {
            StashCreateCommand stash = REPO.stashCreate();
            stashedChanges = true;
            return stash.call();
        }
        catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void applyStash() {

        try {
            System.out.println("Applying stashed changes...");
            REPO.stashApply().call();
        }
        catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Iterable<RevCommit> log(RevFilter filter, java.nio.file.Path path) {

        try {
            String sPath = path.toString().replace("\\", "/");
            return REPO.log().addPath(sPath).setRevFilter(filter).call();
        }
        catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static RevCommit getOriginCommitFor(java.nio.file.Path path) {

        java.util.Iterator<RevCommit> iter = log(RevFilter.ALL, path).iterator();
        RevCommit origin = iter.next();

        while (iter.hasNext()) {
            origin = iter.next();
        }
        return origin;
    }

    public static String getCommitSHA(RevCommit commit) {
        return commit != null ? commit.toObjectId().getName() : "";
    }

    public static java.util.List<DiffEntry> diff(AbstractTreeIterator from, AbstractTreeIterator to,
                                                 OutputStream out, TreeFilter filter) {
        try {
            return REPO.diff().setOldTree(from).setNewTree(to)
                    .setPathFilter(filter).setOutputStream(out).call();
        }
        catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

//    public static void constructDiffCommand(Path path, String from, String to, Path output) {
//
//        String log = "Getting diff for file: \"%s\"%nRevision: \"%s\" -- %s%n";
//        System.out.printf(log, path.getFileName().toString(), from, to);
//
//        String filename = FilenameUtils.removeExtension(path.getFileName().toString()) + ".diff";
//        Path outputPath = Paths.get(output.toString(), filename);
//
//        java.io.File outputFile = outputPath.toFile();
//        java.io.File parentFile = outputFile.getParentFile();
//
//        if (!outputFile.exists()) {
//            try {
//                if (!parentFile.exists() && !parentFile.mkdir() && !outputFile.createNewFile())
//                {
//                    Exception e = new IOException("Unable to create new output file");
//                    throw new IllegalStateException(output.toString(), e);
//                }
//            } catch (IOException e) {
//                throw new IllegalStateException(e);
//            }
//        }
//        git diff $gitBranch:./$3 $2:./$3 > $4
//        runGitBashScript(, to, from, convertToGitPath(path), convertToGitPath(outputPath));
//    }
}

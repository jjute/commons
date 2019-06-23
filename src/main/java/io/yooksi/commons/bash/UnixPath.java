package io.yooksi.commons.bash;

import io.yooksi.commons.define.MethodsNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This object represents a Unix-style path that uses <i>forward slashes</i>. This contrasts with paths
 * in MS-DOS and similar operating systems (such as FreeDOS) and the Microsoft Windows operating systems,
 * in which directories and files are separated  with backslashes. The backslash is an upward-to-the-left
 * sloping straight line character that is a mirror image of the forward slash.
 *
 * <ul style="list-style-type:none">
 *     <li>Unix path: {@code C:/path/to/file}</li>
 *     <li>Windows path: {@code C:\path\to\file}</li>
 * </ul>
 *
 * @see <a href=http://www.linfo.org/forward_slash.html>Forward Slash Definition</a>
 */
@MethodsNotNull
public class UnixPath {

    private final String path;

    private UnixPath(Path path) {
        this.path = path.toString().replace("\\", "/");
    }

    /**
     * Convert a given path into a <i>Unix-style</i> path.
     */
    public static UnixPath get(Path path) {
        return new UnixPath(path);
    }
    /**
     * Convert a given path into a <i>Unix-style</i> path.
     */
    public static UnixPath get(String path) {
        return new UnixPath(Paths.get(path));
    }
    /**
     * Convert this path to a standard Java {@code Path} object.
     */
    public Path convert() {
        return Paths.get(path);
    }
    /**
     * @return a {@code String} representation of this path.
     */
    @Override
    public String toString() {
        return path;
    }
}

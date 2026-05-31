package edu.polytech.filrouge_tp5;

/**
 * Interface contract for fragments that can take or display a picture.
 */
public interface Picturable {
    /**
     * Called when a new picture is captured and stored.
     * @param path The absolute local path to the image file.
     */
    void onPictureTaken(String path);
}

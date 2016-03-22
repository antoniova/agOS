package com.example.user.testone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theone on 3/11/15.
 */
public interface OnFileActionListener {
    void executeSelectedFiles(List<String> selection);
    void editGivenFile(String file);
    void executeGivenFile(String file);
}

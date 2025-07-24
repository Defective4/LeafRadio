package io.github.defective4.springfm.client;

import io.github.defective4.springfm.server.data.ProfileInformation;

@FunctionalInterface
public interface ProfileSelectionCallback {
    void profileSelected(ProfileInformation profile);
}

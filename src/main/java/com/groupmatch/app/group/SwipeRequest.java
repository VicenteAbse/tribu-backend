package com.groupmatch.app.group;

import jakarta.validation.constraints.NotNull;

public class SwipeRequest {

    @NotNull(message = "El campo liked es obligatorio")
    private Boolean liked;

    public SwipeRequest() {}

    public Boolean getLiked() { return liked; }
}

import {Redirect} from "react-router";
import {PAGES} from "../Constants";
import React, {Component} from "react"

export const navigator = (expectedPage, currentPage) => {
    if (expectedPage === currentPage) {
        return null;
    }

    switch (expectedPage) {
        case PAGES.FILE_BROWSER:
            return <Redirect to={"/files"}/>;
        case PAGES.PREVIEWER:
            return <Redirect to={"/preview"}/>;
        case PAGES.LOGIN:
            return <Redirect to={"/login"}/>;
        case PAGES.SIGNUP:
            return <Redirect to={"/signup"}/>;
    }
};

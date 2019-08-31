import $ from "jquery";

import {NETWORK_FAIL_REASONS} from "../Constants";

export const getFilesFromNetwork = (pathUrl) => {
    pathUrl = pathUrl ? pathUrl : "";

    return new Promise((resolve, reject) => {
        $.ajax({
            type: "GET",
            url: "rest/files?path=" + pathUrl,
            contentType: "application/json",
            success: response => resolve(response.files),
            error: response => handleError(reject, response)
        });
    });
};

const handleError = (reject, response) => {
    console.log(response);

    if (response.status === 403 && response.responseText.includes("Authorization is missing")) {
        reject(NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING)
    }
    reject(NETWORK_FAIL_REASONS.OTHER);
}

export const downloadFile = (file) => {
    return new Promise((resolve, reject) => {
        window.open("rest/file?path=" + file.pathUrl, '_blank');
        resolve();
    });
};

export const login = (user, pass) => {
    return new Promise((resolve, reject) => {
        console.log("Attempting login with ", user, " ", pass);
        $.ajax({
            type: "GET",
            headers: {
                'Authorization': 'Basic ' + btoa(user + ':' + pass),
            },
            url: "rest/login",
            success: (result) => {
                resolve();
            },
            error: response => handleError(reject, response)
        });

    });
};

export const signup = (user, pass) => {
    return new Promise((resolve, reject) => {
        console.log("Attempting signup with ", user, " ", pass);
        $.ajax({
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                "username": user,
                "password": pass
            }),
            url: "rest/signup",
            success: (result) => {
                resolve();
            },
            error: response => handleError(reject, response)
        });

    });
};

export const checkServerSession = () => {
    console.log("Checking if server already has a session for this user");
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "GET",
            headers: {},
            url: "rest/login",
            success: (result) => {
                resolve();
            },
            error: response => handleError(reject, response)
        });
    });
};
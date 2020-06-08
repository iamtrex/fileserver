import $ from "jquery";

import {NETWORK_FAIL_REASONS} from "../Constants";
/*
 * Function collection that handles all communciation with Java server.
 * Only src tags like those in videos and image sources hit the server directly.
 */

export const getFilesFromNetwork = (pathUrl) => {
    pathUrl = pathUrl ? pathUrl : "";

    return new Promise((resolve, reject) => {
        $.ajax({
            type: "GET",
            headers: getHeaders(),
            url: "rest/files?path=" + pathUrl,
            contentType: "application/json",
            success: response => resolve(response.files),
            error: response => defaultHandleError(reject, response)
        });
    });
};

export const getImageSourcePath = (file) => {
    return "rest/file?path=" + file.pathUrl;
};

export const getVideoSourcePath = (file) => {
    return "rest/stream?path=" + file.pathUrl;
};

export const getThumbnailBase64 = (path) => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "GET",
            headers: getHeaders(),
            url: "rest/thumbnail?path=" + path,
            success: data => {
                return resolve("data:image/png;base64," + data);
            },
            error: response => defaultHandleError(reject, response)
        });
    });
};

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
            type: "POST",
            headers: {
                "Content-type": "application/x-www-form-urlencoded",
            },
            url: "rest/login",
            data: $.param({"username": user, "password": pass}),
            success: (response) => {
                processLoginResult(resolve, reject, response);
            },
            error: response => defaultHandleError(reject, response)
        });
    });
};

export const logout = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "GET",
            headers: getHeaders(),
            url: "rest/logout",
            success: () => {
                window.localStorage.removeItem("token");
                resolve();
            },
            error: response => defaultHandleError(reject, response)
        });
    });
};

export const signup = (user, pass) => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                "username": user,
                "password": pass
            }),
            url: "rest/signup",
            success: (response) => {
                processLoginResult(resolve, reject, response);
            },
            error: response => defaultHandleError(reject, response)
        });
    });
};

export const checkServerSession = () => {
    console.log("Checking if server already has a session for this user");
    return new Promise((resolve, reject) => {
        $.ajax({
            type: "GET",
            headers: getHeaders(),
            url: "rest/isAuthenticated",
            success: () => {
                resolve();
            },
            error: response => defaultHandleError(reject, response)
        });
    });
};

// TODO SHOULD THIS PLACE SPLIT THE PROMISES OR SHOULD THE ACTION SPLIT IT?
// TODO - handle errors?
export const uploadFiles = (files, path) => {
    return new Promise((resolve, reject) => {
        let promises = [];
        for (let i = 0; i < files.length; i++) {
            let file = files[i];
            promises.push(new Promise((resolve, reject) => {
                const data = new FormData();
                data.append('file', file);
                data.append('name', file.name);

                fetch("rest/upload?path=" + path, {
                    headers: getHeaders(),
                    method: "POST",
                    body: data
                }).then((result) => {
                    resolve();
                });
            }));
        }
        Promise.all(promises).then(() => resolve());
    });
};

const getHeaders = () => {
    let headers = {};

    let token = window.localStorage.getItem("token");
    if (token) {
        headers.Authorization = "Bearer " + token;
    }

    return headers;
};

const defaultHandleError = (reject, response) => {
    console.log(response);
    if (response.status === 401) {
        // Delete cookies and token since if they exist, they are invalid.
        document.cookie = "session-id=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        window.localStorage.removeItem("token");

        // Should re-route to login page.
        reject(NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING);
    }
    reject(NETWORK_FAIL_REASONS.OTHER);
};

const processLoginResult = (resolve, reject, response) => {
    let token = response.token;
    if (!token) {
        defaultHandleError(reject, "No token returned");
    }

    // Store JWT token.
    window.localStorage.setItem("token", token);
    resolve();
};
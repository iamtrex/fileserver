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
};

export const getImageSourcePath = (file) => {
    return "rest/file?path=" + file.pathUrl;
};

export const getVideoSourcePath = (file) => {
    return "rest/stream?path=" + file.pathUrl;
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
            type: "GET",
            headers: {
                'Authorization': 'Basic ' + btoa(user + ':' + pass),
            },
            url: "rest/login",
            success: () => {
                resolve();
            },
            error: response => handleError(reject, response)
        });

    });
};

export const logout = (s) => {
    return new Promise((resolve, reject) => {
        console.log("Attempting logout");
        $.ajax({
            type: "GET",
            url: "rest/logout",
            success: () => {
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
            success: () => {
                resolve();
            },
            error: response => handleError(reject, response)
        });
    });
};

//TODO SHOULD THIS PLACE SPLIT THE PROMISES OR SHOULD THE ACTION SPLIT IT?
export const uploadFiles = (files, path) => {
    return new Promise((resolve, reject) => {
        let promises = [];
        for(let i=0; i<files.length; i++){
            let file = files[i];
            promises.push(new Promise((resolve, reject) => {
                const data = new FormData();
                data.append('file', file);
                data.append('name', file.name);

                fetch("rest/upload?path=" + path, {
                    method: "POST",
                    body: data
                }).then((result) => {
                    resolve();
                });
                /*
                $.ajax({
                    type: "POST",
                    headers: {},
                    url: "rest/upload",
                    data: JSON.stringify({
                        "name": file.name,
                        "fileData": data,
                        "path": path
                    }),
                    success: () => {
                        resolve();
                    },
                    error: response => handleError(reject, response)
                });*/

            }));
        }

        Promise.all(promises).then(() => resolve());
    });
};

import {ACTION, FILE_TYPES, NETWORK_FAIL_REASONS, TYPE_EXTENSIONS} from "../Constants";
import {
    downloadFile,
    getFilesFromNetwork,
    getImageSourcePath,
    getVideoSourcePath,
    uploadFiles
} from "../utils/RestClient";

//TODO need refactoring

export const loadRootDirectory = () => {
    return (dispatch) => {
        dispatchLoadDir(dispatch, "/");
    }
};

export const loadDirectory = (path) => {
    return (dispatch) => {
        dispatchLoadDir(dispatch, path);
    }
};

const dispatchLoadDir = (dispatch, pathUrl) => {
    dispatch({
        type: ACTION.BEGIN_BROWSE_NEW_FILES
    });

    getFilesFromNetwork(pathUrl).then((files) => {
        dispatch({
            type: ACTION.END_BROWSE_NEW_FILES_SUCCESS,
            payload: {
                files: files,
                path: pathUrl
            }
        })
    }).catch((error) => {
        if (error === NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING) {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER,
                payload: {
                    error: error
                }
            })
        } else {
            dispatch({
                type: ACTION.END_BROWSE_NEW_FILES_FAIL,
                payload: {
                    error: error
                }
            })
        }
    });
};

export const attemptPreviewFile = (file) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_PREVIEW_FILE,
        });

        const ext = getExtension(file.name);
        if (TYPE_EXTENSIONS.IMAGE.includes(ext)) {
            getImageSourcePath(file).then((src) => {
                dispatch({
                    type: ACTION.END_PREVIEW_FILE_SUCCESS,
                    payload: {
                        fileName: file.name,
                        src: src,
                        type: FILE_TYPES.IMAGE
                    }
                });
            });
        } else if (TYPE_EXTENSIONS.AUDIO_STREAMABLE.includes(ext)) {

        } else if (TYPE_EXTENSIONS.VIDEO_STREAMABLE.includes(ext)) {
            getVideoSourcePath(file).then((src) => {
                dispatch({
                    type: ACTION.END_PREVIEW_FILE_SUCCESS,
                    payload: {
                        fileName: file.name,
                        src: src,
                        type: FILE_TYPES.VIDEO,
                        videoType: "video/" + ext
                    }
                })
            });
        } else {
            dispatch({
                type: ACTION.FILE_NOT_PREVIEWABLE
            });
        }
    }
};

const attemptDownloadFile = (dispatch, file) => {
    dispatch({
        type: ACTION.BEGIN_DOWNLOAD_FILE,
    });

    downloadFile(file).then(() => {
        dispatch({
            type: ACTION.END_DOWNLOAD_FILE_SUCCESS
        });
    }).catch((error) => {
        if (error === NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING) {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER,
                payload: {
                    error: error
                }
            });
        } else {
            dispatch({
                type: ACTION.END_DOWNLOAD_FILE_FAIL,
                payload: {
                    error: error
                }
            });
        }

    });
};

export const browseUp = (path) => {
    return (dispatch, getState) => {
        if (!path.includes("%2F")) {
            dispatch({
                type: ACTION.BROWSE_FAIL
            });
        }

        const upPath = path.substring(0, path.lastIndexOf("%2F"));
        dispatchLoadDir(dispatch, upPath);
    }
};

export const clickFile = file => {
    console.log("File is ", file);
    return (dispatch) => {
        if (file.type === FILE_TYPES.FOLDER) {
            dispatchLoadDir(dispatch, file.pathUrl);
        } else if (file.type === FILE_TYPES.FILE) {
            attemptDownloadFile(dispatch, file);
        } else {
            dispatch({
                type: ACTION.BROWSE_FAIL
            });
        }
    }
};

const getExtension = name => {
    return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
};

export const stopPreviewing = () => {
    return {
        type: ACTION.END_PREVIEW
    }
};

export const attemptUploadFile = (path, e) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_UPLOAD_FILES,
        });
        let files = e.target.files;
        uploadFiles(files, path).then(() => {
            dispatch({
                type: ACTION.END_UPLOAD_FILES,
                payload: {
                    path: path
                }
            });
        }).catch((error) => {
            console.log("Error", error);
            if (error === NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING) {
                dispatch({
                    type: ACTION.AUTH_REJECTED_FROM_SERVER,
                    payload: {
                        error: error
                    }
                });
            } else {
                dispatch({
                    type: ACTION.END_UPLOAD_FILES_FAIL,
                    payload: {
                        error: error
                    }
                });
            }

        });
    }
};

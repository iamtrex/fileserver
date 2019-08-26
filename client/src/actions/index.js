import {ACTION, FILE_TYPES, NETWORK_FAIL_REASONS} from "../Constants";
import {getFilesFromNetwork, downloadFile, login, checkServerSession} from "../utils/RestClient";

export const loadRootDirectory = () => {
    return (dispatch) => {
        loadDirectory(dispatch, "/");
    }
}

const loadDirectory = (dispatch, pathUrl) => {
    dispatch({
        type: ACTION.BEGIN_BROWSE_NEW_FILES
    });

    getFilesFromNetwork(pathUrl).then((files) => {
        dispatch({
            type: ACTION.END_BROWSE_NEW_FILES_SUCCESS,
            payload: {
                files: files
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
}

const attemptDownloadFile = (dispatch, file) => {
    dispatch({
        type: ACTION.BEGIN_DOWNLOAD_FILE,
    });

    downloadFile(file).then((result) => {
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

export const clickFile = file => {
    return (dispatch) => {
        if (file.type === FILE_TYPES.FOLDER) {
            loadDirectory(dispatch, file.pathUrl);
        } else if (file.type === FILE_TYPES.FILE) {
            attemptDownloadFile(dispatch, file);
        } else {
            dispatch({
                type: ACTION.BROWSE_FAIL
            });
        }
    }
};

export const tryLogin = (user, pass) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_LOGIN
        });

        login(user, pass).then(() => {
            dispatch({
                type: ACTION.AUTHENTICATED_WITH_SERVER
            });
        }).catch((error) => {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER
            });
        });
    }
};

export const checkSession = () => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_CHECK_AUTHENTICATION_WITH_SERVER
        });

        checkServerSession().then(() => {
            dispatch({
                type: ACTION.AUTHENTICATED_WITH_SERVER
            });
        }).catch((error) => {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER
            })
        });
    }
};

export const handleUserChanged = (type, event) => {
    return {
        type: type === 'USER' ? ACTION.USER_CHANGED : ACTION.PASS_CHANGED,
        payload: {
            value: event.target.value
        }
    };
};
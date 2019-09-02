import {ACTION, NETWORK_FAIL_REASONS} from "../Constants";
import {getFilesFromNetwork} from "../utils/RestClient";
import {SET_LOADING_FALSE_ACTION, SET_LOADING_TRUE_ACTION} from "./HelperActions";

export const toggleViewMode = (viewMode) => {
    return {
        type: ACTION.TOGGLE_VIEW_MODE,
        payload: {
            viewMode: viewMode
        }
    }
}
export const browseUp = (currPath) => {
    return (dispatch) => {
        if (!currPath.includes("%2F")) {
            dispatch({
                type: ACTION.BROWSE_FAIL
            });
        }
        const upPath = currPath.substring(0, currPath.lastIndexOf("%2F"));
        attemptLoadDirectory(dispatch, upPath);
    }
};

export const loadDirectory = (dirPath) => {
    return (dispatch) => {
        attemptLoadDirectory(dispatch, dirPath);
    }
};

/**
 * Loads from server the directory and handles errors.
 * @param dispatch
 * @param dirPath
 */
const attemptLoadDirectory = (dispatch, dirPath) => {
    dispatch(SET_LOADING_TRUE_ACTION);

    getFilesFromNetwork(dirPath).then((files) => {
        dispatch({
            type: ACTION.END_BROWSE_NEW_FILES_SUCCESS,
            payload: {
                files: files,
                path: dirPath
            }
        });
        dispatch(SET_LOADING_FALSE_ACTION);
    }).catch((error) => {
        dispatch(SET_LOADING_FALSE_ACTION);

        if (error === NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING) {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER,
                payload: {
                    error: error
                }
            });
        } else {
            dispatch({
                type: ACTION.END_BROWSE_NEW_FILES_FAIL,
                payload: {
                    error: error
                }
            });
        }
    });
};
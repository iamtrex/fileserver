import {ACTION} from "../Constants";
import {attemptShareFiles, getFolderSubcontents} from "../utils/RestClient";
import {SET_LOADING_FALSE_ACTION, SET_LOADING_TRUE_ACTION} from "./HelperActions";
import {handleError} from "./ActionOnErrorHandler";

export const toggleViewMode = (viewMode) => {
    return {
        type: ACTION.TOGGLE_VIEW_MODE,
        payload: {
            viewMode: viewMode
        }
    }
};

export const toggleFileSelected = (fileIndex) => {
    return {
        type: ACTION.TOGGLE_FILE_SELECTED,
        payload: {
            fileIndex: fileIndex
        }
    }
};

export const shareFiles = (files, shareProperties) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.START_UPDATE_SHARE
        });
        attemptShareFiles(files, shareProperties).then((shareIds) => {
            console.log(shareIds);
            dispatch({
                type: ACTION.END_UPDATE_SHARE_SUCCESS,
                payload: {
                    shareIds: shareIds
                }
            })
        }).catch((error) => {
            handleError(dispatch, error, ACTION.END_UPDATE_SHARE_FAIL);
        });
    }
};

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
export const attemptLoadDirectory = (dispatch, dirPath) => {
    dispatch(SET_LOADING_TRUE_ACTION);

    getFolderSubcontents(dirPath).then((files) => {
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
        handleError(dispatch, error, ACTION.END_BROWSE_NEW_FILES_FAIL);
    });
};
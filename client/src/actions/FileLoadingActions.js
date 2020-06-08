import {ACTION} from "../Constants";
import {downloadFile, getThumbnailBase64, uploadFiles} from "../utils/RestClient";
import {SET_LOADING_FALSE_ACTION, SET_LOADING_TRUE_ACTION} from "./HelperActions";
import {handleError} from "./ActionOnErrorHandler";

export const attemptDownloadFile = (file) => {
    return (dispatch) => {
        dispatch(SET_LOADING_TRUE_ACTION);
        downloadFile(file).then(() => {
            dispatch(SET_LOADING_FALSE_ACTION);

            // TODO Should the file be sent to the state?
            dispatch({
                type: ACTION.END_DOWNLOAD_FILE_SUCCESS
            });
        }).catch((error) => {
            handleError(dispatch, error, ACTION.END_DOWNLOAD_FILE_FAIL);
        });
    }
};

export const attemptLoadThumbnail = (file) => {
    return (dispatch) => {
        getThumbnailBase64(file.pathUrl).then((data) => {
            dispatch({
                type: ACTION.LOAD_ICON,
                payload: {
                    src: data,
                    key: file.key
                }
            });
        });
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
            handleError(dispatch, error, ACTION.END_UPLOAD_FILES_FAIL);
        });
    }
};



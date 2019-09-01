import initialState from "../state/InitialState"
import {ACTION, FILE_TYPES} from "../Constants";

export default (state = initialState, action) => {
    switch (action.type) {
        case ACTION.BEGIN_BROWSE_NEW_FILES:
        case ACTION.BEGIN_DOWNLOAD_FILE:
            return {
                ...state,
                isLoading: true
            };
        case ACTION.END_BROWSE_NEW_FILES_SUCCESS:
            return {
                ...state,
                files: action.payload.files,
                path: action.payload.path,
                isLoading: false,
                newFiles: null
            };
        case ACTION.END_BROWSE_NEW_FILES_FAIL:
            return {
                ...state,
                newFiles: null,
                isLoading: false
            }
        case ACTION.END_DOWNLOAD_FILE_FAIL:
        case ACTION.END_DOWNLOAD_FILE_SUCCESS:
            return {
                ...state,
                isLoading: false
            };
        case ACTION.BEGIN_PREVIEW_FILE:
            return {
                ...state,
                isLoading: true
            };
        case ACTION.END_PREVIEW_FILE_SUCCESS:
            if (action.payload.type === FILE_TYPES.IMAGE) {
                return {
                    ...state,
                    isLoading: false,
                    imageSrc: action.payload.src,
                    previewFileType: action.payload.type,
                    previewingFile: true
                }
            } else if (action.payload.type === FILE_TYPES.VIDEO) {
                return {
                    ...state,
                    isLoading: false,
                    videoSrc: action.payload.src,
                    previewFileType: action.payload.type,
                    previewingFile: true,
                    videoType: action.payload.videoType
                }
            }
            console.log("FAIL SHOULD NOT HAPPEN.");
            return {
                ...state,
                isLoading: false,
                previewingFile: false
            };
        case ACTION.FILE_NOT_PREVIEWABLE:
            return {
                ...state,
                isLoading: false,
                previewingFile: false
            };
        case ACTION.END_PREVIEW:
            return {
                ...state,
                previewingFile: false
            }
        case ACTION.BEGIN_UPLOAD_FILES:
            return {
                ...state,
                isUploading: true
            };
        case ACTION.END_UPLOAD_FILES:
            return {
                ...state,
                isUploading: false,
                newFiles: {
                    path: action.payload.path
                }
            };
        case ACTION.END_UPLOAD_FILES_FAIL:
            return {
                ...state,
                isUploading: false,
                newFiles: null
            };
        case ACTION.AUTHENTICATION_MISSING:
        case ACTION.LOGOUT_SUCCESS:
            return {
                ...state,
                files: null,
                path: null,
                isUploading: false,
                previewingFile: false,
                isLoading: false,
                previewFileType: null
            };

    }
    return state;
};

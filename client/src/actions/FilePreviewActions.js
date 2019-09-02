import {ACTION, FILE_TYPES, PAGES, TYPE_EXTENSIONS} from "../Constants";
import {getImageSourcePath, getVideoSourcePath} from "../utils/RestClient";
import {navigateTo} from "./NavigationActions";

export const attemptPreviewFile = (file) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_PREVIEW_FILE,
        });

        const ext = getExtension(file.name);
        if (TYPE_EXTENSIONS.IMAGE.includes(ext)) {
            dispatch({
                type: ACTION.END_PREVIEW_FILE_SUCCESS,
                payload: {
                    fileName: file.name,
                    src: getImageSourcePath(file),
                    type: FILE_TYPES.IMAGE
                }
            });
            dispatch(navigateTo(PAGES.PREVIEWER));
        } else if (TYPE_EXTENSIONS.AUDIO_STREAMABLE.includes(ext)) {
            dispatch({
                type: ACTION.FILE_NOT_PREVIEWABLE
            });
        } else if (TYPE_EXTENSIONS.VIDEO_STREAMABLE.includes(ext)) {
            dispatch({
                type: ACTION.END_PREVIEW_FILE_SUCCESS,
                payload: {
                    fileName: file.name,
                    src: getVideoSourcePath(file),
                    type: FILE_TYPES.VIDEO,
                    videoType: "video/" + ext
                }
            });
            dispatch(navigateTo(PAGES.PREVIEWER));
        } else {
            dispatch({
                type: ACTION.FILE_NOT_PREVIEWABLE
            });
        }
    }
};


const getExtension = name => {
    return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
};
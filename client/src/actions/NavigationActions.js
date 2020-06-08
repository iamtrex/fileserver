import {ACTION} from "../Constants";

export const navigateTo = (page) => {
    return {
        type: ACTION.NAVIGATE_TO,
        payload: {
            "page": page
        }
    }
};
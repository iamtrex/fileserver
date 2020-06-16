import {ACTION} from "../Constants";
import {getUsers} from "../utils/RestClient";
import {handleError} from "./ActionOnErrorHandler";

export const loadUsers = () => {
    return (dispatch) => {
        dispatch({
            type: ACTION.START_LOAD_USERS
        });

        getUsers().then((users) => {
            dispatch({
                type: ACTION.END_LOAD_USERS_SUCCESS,
                payload: users
            })
        }).catch((error) => {
            handleError(dispatch, error, ACTION.END_LOAD_USERS_FAIL);
        });
    }
};
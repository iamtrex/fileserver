import {ACTION, PAGES} from "../Constants";

const INITIAL_STATE = Object.freeze({
    "expectedPage": PAGES.LOGIN,
    "isLoading": false
});

/**
 * Handles all Navigation related states.
 * @param state
 * @param action
 * @returns {{expectedPage: *}}
 */
export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case ACTION.SET_IS_SYNCHRONOUS_LOADING:
            return {
                ...state,
                isLoading: action.payload.isLoading
            };
        case ACTION.NAVIGATE_TO:
            return {
                ...state,
                expectedPage: action.payload.page
            };
        default:
            return state;
    }
};

import {combineReducers} from 'redux'
import FileReducer from "./FileReducer";
import AuthenticationReducer from "./AuthenticationReducer";
import NavigationReducer from "./NavigationReducer";
import ErrorReducer from "./ErrorReducer";

export default combineReducers({
    FileReducer, AuthenticationReducer, NavigationReducer, ErrorReducer
})
import React from 'react';
import {render} from 'react-dom';
import './index.css';
import App from './components/App';
import {Provider} from "react-redux";

import reducers from "./reducers";
import {logger} from 'redux-logger';
import thunk from 'redux-thunk';
import promise from 'redux-promise-middleware';
import {applyMiddleware, createStore} from "redux";

const middleware = applyMiddleware(promise(), thunk, logger);
const store = createStore(reducers, {}, middleware);

render(
    <Provider store={store}>
        <App/>
    </Provider>,
    document.getElementById("root")
);



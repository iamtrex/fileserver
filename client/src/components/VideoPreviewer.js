import {Image} from "semantic-ui-react";

import React from "react"

export const VideoPreviewer = (props) => {
    return <video src={props.src} type="video/webm" width="1280px" height="720px" controls/>
};
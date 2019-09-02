import {Image} from "semantic-ui-react";

import React from "react"

export const VideoPreviewer = (props) => {
    return <video width="100vh" autoplay controls>
        <source src={props.src} type="video/mp4"/>
        <source src={props.src} type="video/webm"/>
    </video>
};
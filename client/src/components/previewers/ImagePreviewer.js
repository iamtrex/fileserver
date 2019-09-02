import {Image} from "semantic-ui-react";

import React from "react"

export const ImagePreviewer = (props) => {
    return <Image className={"image-viewer"}src={props.src}/>
};
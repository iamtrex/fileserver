import {Image} from "semantic-ui-react";
import React from "react";
import styles from "../../style/Previewer.less";

export const ImagePreviewer = (props) => {
    return <Image className={styles.imageViewer} src={props.src}/>
};
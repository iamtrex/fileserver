
import React from "react"
import styles from "../../style/Previewer.less";

export const VideoPreviewer = (props) => {
    return <video className={styles.videoPlayer} autoplay controls>
        <source src={props.src} type="video/mp4"/>
        <source src={props.src} type="video/webm"/>
    </video>
};
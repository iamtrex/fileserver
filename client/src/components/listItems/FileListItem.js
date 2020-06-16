import {Button, Checkbox, Image, List, ListDescription, ListHeader} from "semantic-ui-react";
import {FILE_TYPES} from "../../Constants";

import style from "../../style/FileBrowser.less";

import React from "react"

const verifyOnClick = (elt, evt, callback) => {
    console.log("Clicked div with stuffs =", elt, evt, callback);
    if (evt.target !== this) {
        evt.stopPropagation();
    }
    callback(evt);
};

export const FileListItem = (props) => {
    return <List.Item className={style.listItem} onClick={evt => verifyOnClick(this, evt, props.i.selectionOnClick)}>
        <Checkbox className={style.checkbox} onClick={props.i.onCheckClick} checked={props.i.selected}/>
        {props.i.imageSrc ? <Image src={props.i.imageSrc}/> : null}
        <List.Content>
            <ListHeader className={style.header}>{props.i.header}</ListHeader>
            <ListDescription>{props.i.description}</ListDescription>
        </List.Content>
        {props.i.type === FILE_TYPES.FILE ?
            <List.Content floated={"right"}>
                <Button onClick={props.i.previewOnClick}>Preview</Button>
                <Button onClick={props.i.downloadOnClick}>Download</Button>
            </List.Content> : null
        }
    </List.Item>
};

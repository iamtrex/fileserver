import {Button, Image, List, ListDescription, ListHeader} from "semantic-ui-react";
import {FILE_TYPES} from "../../Constants";

import React from "react"

export const FileListItem = (props) => {
    return <List.Item onClick={props.i.selectionOnClick}>
        {props.i.imageSrc ? <Image src={props.i.imageSrc}/> : null}
        <List.Content>
            <ListHeader><a onClick={props.i.selectionOnClick}>{props.i.header}</a></ListHeader>
            <ListDescription>{props.i.description}</ListDescription>
        </List.Content>
        {props.i.type === FILE_TYPES.FILE ?
            <List.Content floated={"right"}>
                <Button onClick={props.i.previewOnClick}>Preview</Button>
                <Button onClick={props.i.downloadOnClick}>Download</Button>
            </List.Content> : null}
    </List.Item>
};

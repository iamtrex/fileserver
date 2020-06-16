import ScrollTrigger from 'react-scroll-trigger';

import React from "react"
import Card from 'react-bootstrap/Card';

import Button from "react-bootstrap/Button";
import style from "../../style/FileBrowser.less";
import {FILE_TYPES} from "../../Constants";

export const FileThumbItem = (props) => {
    return (
        <Card className={style.cardItem} onClick={props.i.selectionOnClick}>
            <ScrollTrigger onEnter={props.loadThumbnail}>
                {props.i.thumbnailSrc ?
                    <Card.Img className={"mx-auto " + style.cardImage} varient={"top"} src={props.i.thumbnailSrc}/>
                    : null}
            </ScrollTrigger>

            <Card.Body>
                <Card.Title>{props.i.header}</Card.Title>
                <Card.Text>{props.i.description}</Card.Text>
            </Card.Body>
            {props.i.type === FILE_TYPES.FILE ?
                <Card.Footer>
                    <Button onClick={props.i.previewOnClick}>Preview</Button>
                    <Button onClick={props.i.downloadOnClick}>Download</Button>
                </Card.Footer>
                : null
            }
        </Card>
    );
};

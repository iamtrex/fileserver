import ScrollTrigger from 'react-scroll-trigger';

import React from "react"
import Card from 'react-bootstrap/Card';

import Button from "react-bootstrap/Button";

export const FileThumbItem = (props) => {
    return <Card>
        <ScrollTrigger onEnter={props.loadThumbnail}>
            {props.i.thumbnailSrc ?
                <Card.Img varient={"top"} src={props.i.thumbnailSrc} />
                : null}
        </ScrollTrigger>

        <Card.Body>
            <Card.Title><a onClick={props.i.selectionOnClick}>{props.i.header}</a></Card.Title>
            <Card.Text>{props.i.description}</Card.Text>
        </Card.Body>
        <Card.Footer>
            <Button onClick={props.i.previewOnClick}>Preview</Button>
            <Button onClick={props.i.downloadOnClick}>Download</Button>
        </Card.Footer>
    </Card>
    /*
    return <List.Item>
        <List.Content>
            <ScrollTrigger onEnter={props.loadThumbnail}>
                {props.i.thumbnailSrc ?
                    <Image src={props.i.thumbnailSrc}/>
                    : null}
            </ScrollTrigger>
        </List.Content>

        <List.Content floated={"left"}>
            <ListHeader><a onClick={props.i.selectionOnClick}>{props.i.header}</a></ListHeader>
            <ListDescription>{props.i.description}</ListDescription>
        </List.Content>
        {props.i.type === FILE_TYPES.FILE ?
            <List.Content floated={"right"}>
                <Button onClick={props.i.previewOnClick}>Preview</Button>
                <Button onClick={props.i.downloadOnClick}>Download</Button>
            </List.Content> : null}
    </List.Item>
     */
};

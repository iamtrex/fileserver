import React, {Component} from "react"
import Modal from "react-bootstrap/Modal";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import {attemptCreateFolder, onCreateFolderNameChange, showCreateFolderDialog} from "../../actions";
import {connect} from "react-redux";
import {FILE_TYPES} from "../../Constants";

class CreateFolderDialog extends Component {
    constructor(props) {
        super(props);
        this.inputRef = React.createRef();
    }

    onChange = (evt) => {
        let value = evt.target.value;
        let isValid = value && value.trim !== "" &&
            this.props.files.filter(file => file.name === value && file.type === FILE_TYPES.FOLDER).length === 0;
        this.props.onCreateFolderNameChange(isValid);
    };

    onSubmit = () => {
        let name = this.inputRef.current.value;
        this.props.createNewFolder(this.props.path, name);
    };

    render() {
        return (
            <>
                <Modal centered aria-labelledby="contained-modal-title-vcenter"
                       show={this.props.isShow}
                       onHide={this.props.showCreateFolderDialog.bind(this, false)}>
                    <Modal.Header closeButton>
                        <Modal.Title>Modal heading</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Form>
                            <Form.Group controlId="folderName">
                                <Form.Label>Folder Name</Form.Label>
                                <Form.Control required
                                              type="name"
                                              placeholder="Name"
                                              onChange={this.onChange.bind(this)}
                                              isValid={this.props.isValid}
                                              isInvalid={this.props.isInvalid}
                                              ref={this.inputRef}
                                />
                                <Form.Control.Feedback type="invalid">
                                    Please choose a valid folder name.
                                </Form.Control.Feedback>
                                <Form.Text className="text-muted">
                                    Choose a name for your new folder.
                                </Form.Text>
                            </Form.Group>
                        </Form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={this.props.showCreateFolderDialog.bind(this, false)}>
                            Close
                        </Button>
                        <Button variant="primary"
                                onClick={this.onSubmit}>
                            Create
                        </Button>
                    </Modal.Footer>
                </Modal>
            </>
        );
    }

};

const mapStateToProps = state => ({
    isShow: state.FileReducer.isCreateFolderDialogShowing,
    isValid: state.FileReducer.isCreateFolderNameValid,
    isInvalid: state.FileReducer.isCreateFolderNameInvalid,
    files: state.FileReducer.files,
    path: state.FileReducer.path
});

const mapDispatchToProps = dispatch => ({
    createNewFolder: (path, name) => dispatch(attemptCreateFolder(path, name)),
    showCreateFolderDialog: (show) => dispatch(showCreateFolderDialog(show)),
    onCreateFolderNameChange: (isValid) => dispatch(onCreateFolderNameChange(isValid))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(CreateFolderDialog)

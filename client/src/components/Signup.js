import "../style/FileBrowser.less"
import React, {Component} from "react"

import {checkSession, handleSignupUserChanged, trySignup} from "../actions"
import {connect} from "react-redux"
import {Button, Form, Grid, Header, Image, Segment} from 'semantic-ui-react'
import {Redirect} from "react-router";


class Signup extends Component {

    componentDidMount() {
        if (!this.props.hasCheckedServerAuth) {
            this.props.checkSession();
        }
    }

    doSignup = () => {
        if (this.props.password !== this.props.confirmPassword) {
            // Throw error to UI.
        } else {
            this.props.trySignup(this.props.username, this.props.password);
        }
    };

    render() {
        return !this.props.isUserAuthenticated ?
            <Grid textAlign='center' style={{height: '100vh'}} verticalAlign='middle'>
                <Grid.Column style={{maxWidth: 450}}>
                    <Header as='h2' color='teal' textAlign='center'>
                        <Image src='/logo.png'/> Log-in to your account
                    </Header>
                    <Form size='large'>
                        <Segment stacked>
                            <Form.Input fluid icon='user' iconPosition='left' placeholder='Username'
                                        onChange={this.props.handleSignupUserChanged.bind(this, 'USER')}/>
                            <Form.Input
                                fluid
                                icon='lock'
                                iconPosition='left'
                                placeholder='Password'
                                type='password'
                                onChange={this.props.handleSignupUserChanged.bind(this, 'PASSWORD')}
                            />

                            <Form.Input
                                fluid
                                icon='lock'
                                iconPosition='left'
                                placeholder='Confirm Password'
                                type='password'
                                onChange={this.props.handleSignupUserChanged.bind(this, 'CONFIRM_PASSWORD')}
                            />

                            <Button color='teal' fluid size='large' onClick={this.doSignup.bind(this)}>
                                Sign Up!
                            </Button>
                        </Segment>
                    </Form>
                </Grid.Column>
            </Grid> :
            <Redirect to={"/files"}/>
    }
}


const mapStateToProps = state => ({
    username: state.UserReducer.signupUsername,
    password: state.UserReducer.signupPassword,
    confirmPassword: state.UserReducer.signupConfirmPassword,
    isUserAuthenticated: state.UserReducer.isUserAuthenticated,
    hasCheckedServerAuth: state.UserReducer.hasCheckedServerAuth
});

const mapDispatchToProps = dispatch => ({
    checkSession: () => dispatch(checkSession()),
    trySignup: (user, pass) => dispatch(trySignup(user, pass)),
    handleSignupUserChanged: (type, event) => dispatch(handleSignupUserChanged(type, event))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Signup)

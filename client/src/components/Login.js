import "../style/FileBrowser.less"
import React, {Component} from "react"

import {checkSession, handleUserChanged, tryLogin} from "../actions"
import {connect} from "react-redux"
import {Button, Form, Grid, Header, Image, Message, Segment} from 'semantic-ui-react'
import {Redirect} from "react-router";
import {Link} from "react-router-dom";


class Login extends Component {

    componentDidMount() {
        if (!this.props.hasCheckedServerAuth) {
            this.props.checkSession();
        }
    }


    doLogin = () => {
        this.props.tryLogin(this.props.username, this.props.password);
    };

    render() {
        return !this.props.isUserAuthenticated ?
            <Grid textAlign='center' style={{height: '100vh'}} verticalAlign='middle'>
                <Grid.Column style={{maxWidth: 450}}>
                    <Header as='h2' color='teal' textAlign='center'>
                        <Image src='/logo512.png'/> Log-in to your account
                    </Header>
                    <Form size='large'>
                        <Segment stacked>
                            <Form.Input fluid icon='user' iconPosition='left' placeholder='Username'
                                        onChange={this.props.handleUserChanged.bind(this, 'USER')}/>
                            <Form.Input
                                fluid
                                icon='lock'
                                iconPosition='left'
                                placeholder='Password'
                                type='password'
                                onChange={this.props.handleUserChanged.bind(this, 'PASSWORD')}
                            />

                            <Button color='teal' fluid size='large' onClick={this.doLogin.bind(this)}>
                                Login
                            </Button>
                        </Segment>
                    </Form>
                    <Message>
                        New to us? <Link to={"/signup"}><Button size="mini" content={"Sign Up!"}/></Link>
                    </Message>
                </Grid.Column>
            </Grid> :
            <Redirect to={"/files"}/>
    }
}


const mapStateToProps = state => ({
    username: state.UserReducer.username,
    password: state.UserReducer.password,
    isUserAuthenticated: state.UserReducer.isUserAuthenticated,
    hasCheckedServerAuth: state.UserReducer.hasCheckedServerAuth
});

const mapDispatchToProps = dispatch => ({
    checkSession: () => dispatch(checkSession()),
    tryLogin: (user, pass) => dispatch(tryLogin(user, pass)),
    handleUserChanged: (type, event) => dispatch(handleUserChanged(type, event))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Login)

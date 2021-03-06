import React, { Component } from 'react';
import {StyleSheet, Text, TextInput, View, Image, Alert} from 'react-native';

export default class UpdateScope extends Component {
  shouldComponentUpdate(nextProps){
    if(this.props.update){
      return this.props.update();
    }
    if(!('equals' in this.props)) throw new Error("You should define 'equals' prop (function).");
    if(!('value' in this.props)) throw new Error("You should define 'value' prop (object).");
    let result = !this.props.equals(this.props.value, nextProps.value);
    return result;
  }
  render(){
      return (<View {... this.props} >{this.props.children}</View>);
  }
}


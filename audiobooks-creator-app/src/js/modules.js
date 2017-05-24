import React, { Component } from 'react';
import { Text, View } from 'react-native';

import IconFA, {glyphMap as glyphMapFA} from 'react-native-vector-icons/FontAwesome';
import IconMD, {glyphMap as glyphMapMD} from 'react-native-vector-icons/MaterialIcons';
import IconIO, {glyphMap as glyphMapIO} from 'react-native-vector-icons/Ionicons';

export const icons = {IconFA, IconMD, glyphMapFA, glyphMapMD};

export const TabIcon = (name) => (
    ({tintColor, focused}) => (<View style={{flex:1,
                                             // paddingTop: 6,
                                             // backgroundColor: 'red',
                                             alignItems:'center',
                                             justifyContent: 'center',
                                             flexDirection: 'row'}}>
                               <IconIO size={16} name={name} color={tintColor}/></View>));

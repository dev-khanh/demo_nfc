import React, { useState, useEffect } from 'react';
import { Button, Text, StyleSheet, TextInput, View, NativeModules, NativeEventEmitter } from 'react-native';

const App = () => {
  const [visibleStatusBar, setVisibleStatusBar] = useState(false);
  const [textState, setTextState] = useState('');
  const { NFCModules } = NativeModules;


  useEffect(() => {
    NFCModules.init();
    console.log('DEVK INIT');
    const EVENT_NAME = new NativeEventEmitter(NativeModules.ChatMessageManager);
    this.subscription = EVENT_NAME.addListener(
      'INFO_EVENT',
      (message) => {
        console.log("DEVK MESSAGE REACT NATIVE", message);
      });
  });


  const onChangeText = (text) => {
    NFCModules.emitAddress(text);
    setTextState(text);
  }

  return (
    <View style={styles.container}>
      <View>
        <Text style={styles.textStyle}>
          Status: {!visibleStatusBar ? 'Visible' : 'Hidden'}
        </Text>
      </View>
      <View style={styles.buttonContainer}>
        <TextInput
          style={styles.viewInput}
          onChangeText={(text) => onChangeText(text)}
          value={textState}
        />
      </View>
      <View style={styles.buttonContainer}>
        <Button
          title="connect"
          onPress={() => NFCModules.connect()}
        />
      </View>
      <View style={styles.buttonContainer}>
        <Button
          title="disconnect"
          onPress={() => NFCModules.disConnect()}
        />
      </View>
      <View style={styles.buttonContainer}>
        <Button
          title="reconnect"
          onPress={() => NFCModules.reConnect()}
        />
      </View>
      <View style={styles.buttonContainer}>
        <Button
          title="read tag"
          onPress={() => NFCModules.readTag()}
        />
      </View>
      <View style={styles.buttonContainer}>
        <Button
          title="exit"
          onPress={() => NFCModules.exit()}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingTop: 20,
    backgroundColor: '#ECF0F1',
    padding: 8,
  },
  buttonContainer: {
    padding: 10,
  },
  textStyle: {
    textAlign: 'center',
  },
  viewInput: {
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
  },
});

export default App;
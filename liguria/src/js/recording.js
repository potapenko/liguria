import {
    AudioRecorder,
    AudioUtils
} from 'react-native-audio';

const audioPath = AudioUtils.DocumentDirectoryPath + '/recordign.aac';

AudioRecorder.prepareRecordingAtPath(audioPath, {
    SampleRate: 22050,
    Channels: 2,
    AudioQuality: "High",
    AudioEncoding: "aac",
    MeteringEnabled: true
});

export default links = { AudioRecorder, audioPath };

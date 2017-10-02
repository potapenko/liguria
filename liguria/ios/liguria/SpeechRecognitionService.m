#import "SpeechRecognitionService.h"

#import <GRPCClient/GRPCCall.h>
#import <RxLibrary/GRXBufferedPipe.h>
#import <ProtoRPC/ProtoRPC.h>

#define API_KEY @"AIzaSyB-jVfHSdawi_bvC4_NPJByQCZCzpRdZKI"
#define HOST @"speech.googleapis.com"

@interface SpeechRecognitionService ()

@property (nonatomic, assign) BOOL streaming;
@property (nonatomic, strong) Speech *client;
@property (nonatomic, strong) GRXBufferedPipe *writer;
@property (nonatomic, strong) GRPCProtoCall *call;

// @property (nonatomic, assign) NSArray<NSString *> *phrasesArray;

@end

@implementation SpeechRecognitionService

+ (instancetype) sharedInstance {
  static SpeechRecognitionService *instance = nil;
  if (!instance) {
    instance = [[self alloc] init];
    instance.sampleRate = 16000.0; // default value
  }
  return instance;
}

- (void) streamAudioData:(NSData *) audioData
          withCompletion:(SpeechRecognitionCompletionHandler)completion {

  if (!_streaming) {
    // if we aren't already streaming, set up a gRPC connection
    _client = [[Speech alloc] initWithHost:HOST];
    _writer = [[GRXBufferedPipe alloc] init];
    _call = [_client RPCToStreamingRecognizeWithRequestsWriter:_writer
                                         eventHandler:^(BOOL done, StreamingRecognizeResponse *response, NSError *error) {
                                           completion(response, error);
                                         }];

    // authenticate using an API key obtained from the Google Cloud Console
    _call.requestHeaders[@"X-Goog-Api-Key"] = API_KEY;
    // if the API key has a bundle ID restriction, specify the bundle ID like this
    _call.requestHeaders[@"X-Ios-Bundle-Identifier"] = [[NSBundle mainBundle] bundleIdentifier];

    NSLog(@"HEADERS: %@", _call.requestHeaders);

    [_call start];
    _streaming = YES;

    // send an initial request message to configure the service
    RecognitionConfig *recognitionConfig = [RecognitionConfig message];
    recognitionConfig.encoding = RecognitionConfig_AudioEncoding_Linear16;
    recognitionConfig.sampleRateHertz = self.sampleRate;
    recognitionConfig.languageCode = @"ru-RU";
    recognitionConfig.maxAlternatives = 1;
    recognitionConfig.enableWordTimeOffsets = YES;

    if(_phrasesArray){
     SpeechContext *context = [SpeechContext message];
      NSMutableArray *mutablePhrasesArray = [[NSMutableArray alloc] initWithArray:self.phrasesArray];
      context.phrasesArray = mutablePhrasesArray;
     recognitionConfig.speechContextsArray = @[context];
    }
    
    StreamingRecognitionConfig *streamingRecognitionConfig = [StreamingRecognitionConfig message];
    streamingRecognitionConfig.config = recognitionConfig;
    streamingRecognitionConfig.singleUtterance = NO;
    streamingRecognitionConfig.interimResults = YES;

    StreamingRecognizeRequest *streamingRecognizeRequest = [StreamingRecognizeRequest message];
    streamingRecognizeRequest.streamingConfig = streamingRecognitionConfig;

    [_writer writeValue:streamingRecognizeRequest];
  }

  // send a request message containing the audio data
  StreamingRecognizeRequest *streamingRecognizeRequest = [StreamingRecognizeRequest message];
  streamingRecognizeRequest.audioContent = audioData;
  [_writer writeValue:streamingRecognizeRequest];
}

- (void) stopStreaming {
  if (!_streaming) {
    return;
  }
  [_writer finishWithError:nil];
  _streaming = NO;
}

- (BOOL) isStreaming {
  return _streaming;
}

@end

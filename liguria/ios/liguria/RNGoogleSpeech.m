#import <AVFoundation/AVFoundation.h>

#import "RNGoogleSpeech.h"
#import "AudioController.h"
#import "SpeechRecognitionService.h"
#import "google/cloud/speech/v1/CloudSpeech.pbrpc.h"
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>

#define SAMPLE_RATE 16000.0f

@interface RNGoogleSpeech () <AudioControllerDelegate>

@property (nonatomic, strong) NSMutableData *audioData;
@property (nonatomic, assign) BOOL inProgress;

@property (nonatomic, weak, readwrite) RCTBridge *bridge;

@end

@implementation RNGoogleSpeech


RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(startRecognizing :(NSArray<NSString *> *) phrasesArray) {
  
  if(_inProgress){
    NSLog(@">! RECOGNIZING ALREADY STARTED!");
    return;
  }
  
  NSLog(@">! START RECOGNIZING");
  
  _inProgress = TRUE;
  
  [AudioController sharedInstance].delegate = self;

  AVAudioSession *audioSession = [AVAudioSession sharedInstance];
  [audioSession setCategory:AVAudioSessionCategoryRecord error:nil];

  self.audioData = [[NSMutableData alloc] init];
  
  [[AudioController sharedInstance] prepareWithSampleRate:SAMPLE_RATE];
  [[SpeechRecognitionService sharedInstance] setSampleRate:SAMPLE_RATE];
  [[SpeechRecognitionService sharedInstance] setPhrasesArray:phrasesArray];
  [[AudioController sharedInstance] start];
}

RCT_EXPORT_METHOD(stopRecognizing) {
  [[AudioController sharedInstance] stop];
  [[SpeechRecognitionService sharedInstance] stopStreaming];
  _inProgress = FALSE;
}

- (void) processSampleData:(NSData *)data
{
  [self.audioData appendData:data];
  NSInteger frameCount = [data length] / 2;
  int16_t *samples = (int16_t *) [data bytes];
  int64_t sum = 0;
  for (int i = 0; i < frameCount; i++) {
    sum += abs(samples[i]);
  }
  NSLog(@"audio %d %d", (int) frameCount, (int) (sum * 1.0 / frameCount));

  // We recommend sending samples in 100ms chunks
  int chunk_size = 0.1 /* seconds/chunk */ * SAMPLE_RATE * 2 /* bytes/sample */ ; /* bytes/chunk */

  if ([self.audioData length] > chunk_size) {
    NSLog(@">! SENDING");
    [[SpeechRecognitionService sharedInstance]
     streamAudioData:self.audioData
      withCompletion:^(StreamingRecognizeResponse *response, NSError *error) {
        NSMutableDictionary* result = [[NSMutableDictionary alloc] init];

        if (error) {
          [self stopRecognizing];
          result[@"error"] = [error localizedDescription];
        } else if (response) {
          NSLog(@">! RESPONSE: %@", response);
          /*
          for (StreamingRecognitionResult *oneResult in response.resultsArray) {
          }*/
          
          StreamingRecognitionResult * _Nullable firstResult = response.resultsArray.firstObject;
          SpeechRecognitionAlternative * _Nullable firstAlternative = firstResult.alternativesArray.firstObject;
          NSDictionary *body = @{@"transcript":  firstAlternative.transcript,
                                 @"isFinal": @(firstResult.isFinal)};
          if(firstResult.isFinal){
            
          }
          [self.bridge.eventDispatcher sendAppEventWithName:@"GoogleRecognizeResult"
                                                       body:body];
        }
      }
   ];
    self.audioData = [[NSMutableData alloc] init];
  }
}

@end

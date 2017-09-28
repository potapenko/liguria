#import <AVFoundation/AVFoundation.h>

#import "RNGoogleSpeech.h"
#import "AudioController.h"
#import "SpeechRecognitionService.h"
#import "google/cloud/speech/v1/CloudSpeech.pbrpc.h"

#define SAMPLE_RATE 16000.0f

@interface RNGoogleSpeech () <AudioControllerDelegate>
@property (nonatomic, strong) NSMutableData *audioData;
@property (nonatomic, strong) RCTResponseSenderBlock callback;
@end

@implementation RNGoogleSpeech

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(startRecognizing
                  :(NSArray<NSString *> *) phrasesArray
                  :(RCTResponseSenderBlock)callback) {

  [AudioController sharedInstance].delegate = self;

  AVAudioSession *audioSession = [AVAudioSession sharedInstance];
  [audioSession setCategory:AVAudioSessionCategoryRecord error:nil];

  self.callback = callback;
  self.audioData = [[NSMutableData alloc] init];
  [[AudioController sharedInstance] prepareWithSampleRate:SAMPLE_RATE];
  [[SpeechRecognitionService sharedInstance] setSampleRate:SAMPLE_RATE];
  [[AudioController sharedInstance] start];
}

RCT_EXPORT_METHOD(stopRecognizing) {
  [[AudioController sharedInstance] stop];
  [[SpeechRecognitionService sharedInstance] stopStreaming];
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
    NSLog(@"SENDING");
    [[SpeechRecognitionService sharedInstance] streamAudioData:self.audioData
                                                withCompletion:^(StreamingRecognizeResponse *response, NSError *error) {
                                                  NSMutableDictionary* result = [[NSMutableDictionary alloc] init];

                                                  if (error) {
                                                    [self stopRecognizing];
                                                    result[@"error"] = [error localizedDescription];
                                                  } else if (response) {



                                                    BOOL finished = NO;
                                                    NSLog(@"RESPONSE: %@", response);
                                                    for (StreamingRecognitionResult *result in response.resultsArray) {
                                                      if (result.isFinal) {
                                                        finished = YES;
                                                      }
                                                    }
                                                   // _textView.text = [response description];
                                                    if (finished) {
                                                      [self stopRecognizing];
                                                    }
                                                  }
                                                }
     ];
    self.audioData = [[NSMutableData alloc] init];
  }
}

@end

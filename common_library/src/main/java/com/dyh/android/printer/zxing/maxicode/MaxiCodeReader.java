/*
 * Copyright 2011 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dyh.android.printer.zxing.maxicode;

import com.dyh.android.printer.zxing.datamatrix.DataMatrixReader;
import com.dyh.android.printer.zxing.qrcode.QRCodeReader;
import com.dyh.android.printer.zxing.BarcodeFormat;
import com.dyh.android.printer.zxing.BinaryBitmap;
import com.dyh.android.printer.zxing.ChecksumException;
import com.dyh.android.printer.zxing.DecodeHintType;
import com.dyh.android.printer.zxing.FormatException;
import com.dyh.android.printer.zxing.NotFoundException;
import com.dyh.android.printer.zxing.Reader;
import com.dyh.android.printer.zxing.Result;
import com.dyh.android.printer.zxing.ResultMetadataType;
import com.dyh.android.printer.zxing.ResultPoint;
import com.dyh.android.printer.zxing.common.BitMatrix;
import com.dyh.android.printer.zxing.common.DecoderResult;
import com.dyh.android.printer.zxing.maxicode.decoder.Decoder;

import java.util.Map;

/**
 * This implementation can detect and decode a MaxiCode in an image.
 */
public final class MaxiCodeReader implements Reader {

  private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
  private static final int MATRIX_WIDTH = 30;
  private static final int MATRIX_HEIGHT = 33;

  private final Decoder decoder = new Decoder();

  /*
  Decoder getDecoder() {
    return decoder;
  }
   */

  /**
   * Locates and decodes a MaxiCode in an image.
   *
   * @return a String representing the content encoded by the MaxiCode
   * @throws NotFoundException if a MaxiCode cannot be found
   * @throws FormatException if a MaxiCode cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
    return decode(image, null);
  }

  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, ChecksumException, FormatException {
    DecoderResult decoderResult;
    if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
      BitMatrix bits = extractPureBits(image.getBlackMatrix());
      decoderResult = decoder.decode(bits, hints);
    } else {
      throw NotFoundException.getNotFoundInstance();
    }

    ResultPoint[] points = NO_POINTS;
    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.MAXICODE);

    String ecLevel = decoderResult.getECLevel();
    if (ecLevel != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
    }
    return result;
  }

  @Override
  public void reset() {
    // do nothing
  }

  /**
   * This method detects a code in a "pure" image -- that is, pure monochrome image
   * which contains only an unrotated, unskewed, image of a code, with some white border
   * around it. This is a specialized method that works exceptionally fast in this special
   * case.
   *
   * @see DataMatrixReader#extractPureBits(BitMatrix)
   * @see QRCodeReader#extractPureBits(BitMatrix)
   */
  private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
    
    int[] enclosingRectangle = image.getEnclosingRectangle();
    if (enclosingRectangle == null) {
      throw NotFoundException.getNotFoundInstance();
    }
    
    int left = enclosingRectangle[0];
    int top = enclosingRectangle[1];
    int width = enclosingRectangle[2];
    int height = enclosingRectangle[3];

    // Now just read off the bits
    BitMatrix bits = new BitMatrix(MATRIX_WIDTH, MATRIX_HEIGHT);
    for (int y = 0; y < MATRIX_HEIGHT; y++) {
      int iy = top + (y * height + height / 2) / MATRIX_HEIGHT;
      for (int x = 0; x < MATRIX_WIDTH; x++) {
        int ix = left + (x * width + width / 2 + (y & 0x01) *  width / 2) / MATRIX_WIDTH;
        if (image.get(ix, iy)) {
          bits.set(x, y);
        }
      }
    }
    return bits;
  }

}

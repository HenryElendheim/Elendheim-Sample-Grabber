package com.elendheim.samplegrabber.audio;

import java.io.ByteArrayOutputStream;

import de.sciss.jump3r.mp3.BitStream;
import de.sciss.jump3r.mp3.GainAnalysis;
import de.sciss.jump3r.mp3.GetAudio;
import de.sciss.jump3r.mp3.ID3Tag;
import de.sciss.jump3r.mp3.Lame;
import de.sciss.jump3r.mp3.LameGlobalFlags;
import de.sciss.jump3r.mp3.MPEGMode;
import de.sciss.jump3r.mp3.Parse;
import de.sciss.jump3r.mp3.Presets;
import de.sciss.jump3r.mp3.Quantize;
import de.sciss.jump3r.mp3.QuantizePVT;
import de.sciss.jump3r.mp3.Reservoir;
import de.sciss.jump3r.mp3.Takehiro;
import de.sciss.jump3r.mp3.VBRTag;
import de.sciss.jump3r.mp3.Version;
import de.sciss.jump3r.mpg.Common;
import de.sciss.jump3r.mpg.Interface;
import de.sciss.jump3r.mpg.MPGLib;

/**
 * Encodes mono 16-bit PCM to CBR MP3 using the pure-Java jump3r port of
 * LAME. The module wiring mirrors jump3r's own LameEncoder, minus its
 * javax.sound dependency, which does not exist on Android.
 */
public final class Mp3Encoder {

    private Mp3Encoder() {
    }

    public static byte[] encode(short[] samples, int sampleRate, int bitrateKbps) {
        Lame lame = new Lame();
        GainAnalysis ga = new GainAnalysis();
        BitStream bs = new BitStream();
        Presets p = new Presets();
        QuantizePVT qupvt = new QuantizePVT();
        Quantize qu = new Quantize();
        VBRTag vbr = new VBRTag();
        Version ver = new Version();
        ID3Tag id3 = new ID3Tag();
        Reservoir rv = new Reservoir();
        Takehiro tak = new Takehiro();
        Parse parse = new Parse();
        MPGLib mpg = new MPGLib();
        Interface intf = new Interface();
        Common common = new Common();
        GetAudio gaud = new GetAudio();

        lame.setModules(ga, bs, p, qupvt, qu, vbr, ver, id3, mpg);
        bs.setModules(ga, mpg, ver, vbr);
        id3.setModules(bs, ver);
        p.setModules(lame);
        qu.setModules(bs, rv, qupvt, tak);
        qupvt.setModules(tak, rv, lame.enc.psy);
        rv.setModules(bs);
        tak.setModules(qupvt);
        vbr.setModules(lame, bs, ver);
        gaud.setModules(parse, mpg);
        parse.setModules(ver, id3, p);
        mpg.setModules(intf, common);
        intf.setModules(vbr, common);

        LameGlobalFlags gfp = lame.lame_init();
        gfp.num_channels = 1;
        gfp.in_samplerate = sampleRate;
        gfp.mode = MPEGMode.MONO;
        gfp.brate = bitrateKbps;
        gfp.quality = 5;
        gfp.bWriteVbrTag = false;
        id3.id3tag_init(gfp);
        gfp.write_id3tag_automatic = false;
        gfp.findReplayGain = false;

        int rc = lame.lame_init_params(gfp);
        if (rc < 0) {
            throw new IllegalStateException("MP3 encoder init failed: " + rc);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int chunk = 1152 * 4;
        byte[] mp3 = new byte[chunk * 2 + 7200];
        int[] left = new int[chunk];
        int[] right = new int[chunk];
        int pos = 0;
        while (pos < samples.length) {
            int n = Math.min(chunk, samples.length - pos);
            for (int i = 0; i < n; i++) {
                int v = samples[pos + i] << 16;
                left[i] = v;
                right[i] = v;
            }
            int bytes = lame.lame_encode_buffer_int(gfp, left, right, n, mp3, 0, mp3.length);
            if (bytes < 0) {
                throw new IllegalStateException("MP3 encode failed: " + bytes);
            }
            out.write(mp3, 0, bytes);
            pos += n;
        }
        int bytes = lame.lame_encode_flush(gfp, mp3, 0, mp3.length);
        if (bytes > 0) {
            out.write(mp3, 0, bytes);
        }
        lame.lame_close(gfp);
        return out.toByteArray();
    }
}

package mrwint.gbtasgen.segment.pokemon.gen1.rta;

import mrwint.gbtasgen.rom.pokemon.gen1.RedRomInfo;
import mrwint.gbtasgen.segment.SingleGbSegment;
import mrwint.gbtasgen.util.SingleGbRunner;

import java.io.IOException;

public class RedRTA extends SingleGbSegment {

    @Override
    protected void execute() {
        seq(new ContinueFromSave());
//        save("rta-testsav");
/*        seq(new OakSpeech());
        save("rta-oakspeech");

        load("rta-oakspeech");
        seq(new ChooseSquirtle());
        save("rta-choosesquirtle");

        load("rta-choosesquirtle");
        seq(new Rival1Fight());
        save("rta-rival1fight");

        load("rta-rival1fight");
        seq(new OaksParcel());
        save("rta-oaksparcel");

        load("rta-oaksparcel");
        seq(new ViridianShopping());
        save("rta-viridianshopping");

        load("rta-viridianshopping");
*/
        seq(new NidoFBEE());
        save("rta-nidoFBEE");

    }

    public static void main(String[] args) throws IOException {
        SingleGbRunner.run(new RedRomInfo(), new RedRTA());
    }
}

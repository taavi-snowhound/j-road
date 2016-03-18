package com.nortal.jroad.client.pkr;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;

import com.nortal.jroad.client.pkr.types.ee.riik.xtee.pkr.producers.producer.pkr.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;

import com.nortal.jroad.client.exception.XTeeServiceConsumptionException;
import com.nortal.jroad.client.service.XTeeDatabaseService;
import com.nortal.jroad.client.pkr.types.ee.riik.xtee.pkr.producers.producer.pkr.Tkis1Paring;
import com.nortal.jroad.client.pkr.types.ee.riik.xtee.pkr.producers.producer.pkr.Tkis1Vastus;
import com.nortal.jroad.client.pkr.types.ee.riik.xtee.pkr.producers.producer.pkr.TtaPensionToetusParing;
import com.nortal.jroad.client.pkr.types.ee.riik.xtee.pkr.producers.producer.pkr.TtaPensionToetusVastus;
import com.nortal.jroad.client.service.v2.XTeeDatabaseService;
import com.nortal.jroad.client.service.callback.CustomCallback;
import com.nortal.jroad.client.service.extractor.CustomExtractor;
import com.nortal.jroad.model.XTeeMessage;
import com.nortal.jroad.model.XmlBeansXTeeMessage;
import com.nortal.jroad.util.SOAPUtil;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Node;

/**
 * <code>PKR</code> or <code>TPKR</code> X-tee service<br>
 * This implementation make test or production request. Generated test and production message are the same, except
 * namespace NS5 that will replaces to test or production
 * 
 * @author Margus Hanni
 */
public class PkrXTeeServiceImpl extends XTeeDatabaseService implements PkrXTeeService {

  private static final String TEST_DATABASE = "tpkr";

  private static final String PENSION_TOETUS = "tta_pension_toetus";
  private static final String TKIS1 = "tkis1";
  private static final String TKIS2 = "tkis2";

  private final boolean useTestDatabase;

  public PkrXTeeServiceImpl() {
    this(false);
  }

  public PkrXTeeServiceImpl(boolean useTestDatabase) {
    this.useTestDatabase = useTestDatabase;
  }

  public TtaPensionToetusVastus getPensionToetus(String isikukood) throws XTeeServiceConsumptionException {

    TtaPensionToetusParing paring = TtaPensionToetusParing.Factory.newInstance();
    paring.setIsikukood(isikukood);

    XTeeMessage<TtaPensionToetusVastus> response =
        send(new XmlBeansXTeeMessage<TtaPensionToetusParing>(paring),
             PENSION_TOETUS,
             null,
             new PkrCallback(),
             new PkrExtractor());

    return response.getContent();
  }
  
  public Tkis1Vastus getTkis1(String isikukood, Date algusKuup, Date loppKuup,
      Date kuup) throws XTeeServiceConsumptionException {

    Tkis1Paring paring = Tkis1Paring.Factory.newInstance();
    paring.setIsikukood(isikukood);
    paring.setAlgusKuup(getCalendar(algusKuup));
    paring.setLoppKuup(getCalendar(loppKuup));
    paring.setKuup(getCalendar(kuup));

    XTeeMessage<Tkis1Vastus> response = send(
        new XmlBeansXTeeMessage<Tkis1Paring>(paring), TKIS1, null,
        new PkrCallback(), new PkrExtractor());

    return response.getContent();
  }

  public Tkis2Valjund getTkis2V1(String isikukood, Date algusKuup, Date loppKuup) throws XTeeServiceConsumptionException {
    Tkis2Sisend sisend = Tkis2Sisend.Factory.newInstance();
    sisend.setIsikukood(isikukood);
    sisend.setPerAlgus(getCalendar(algusKuup));
    sisend.setPerLopp(getCalendar(loppKuup));

    XTeeMessage<Tkis2Valjund> response = send(new XmlBeansXTeeMessage<Tkis2Sisend>(sisend), TKIS2, "v1");
    return response.getContent();
  }

  private Calendar getCalendar(Date kuup) {
    if (kuup == null ) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(kuup);
    return cal;
  }

  private class PkrCallback extends CustomCallback {

    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
      callback.doWithMessage(message);

      if (useTestDatabase) {
        SOAPUtil.substitute(message, getDatabase(), TEST_DATABASE);
      }
    }
  }

  public class PkrExtractor extends CustomExtractor {

    public Object extractData(WebServiceMessage message) throws IOException, TransformerException {
      if (useTestDatabase) {
        SOAPUtil.substitute(message, TEST_DATABASE, getDatabase());
      }

      return extractor.extractData(message);
    }
  }

}
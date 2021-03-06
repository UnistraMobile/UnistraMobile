package com.mobile.unistra.unistramobile.calendrier;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Alexandre on 31-03-15.
 */
public class LocalCal {
    private ArrayList<Event> agendaLocal;
    private String selectedCalendarId;
    private String ressource;

    /**
     * Récupère la liste des événements dans l'agendaLocal
     * @return Une ArrayList de Event.
     */
    public ArrayList<Event> getEvents(){
        return agendaLocal;
    }

    /**
     * Supprime un événement du calendrier enregistré
     * @param e Evenement à retirer de l'agenda.
     */
    public void remove(Event e){
        agendaLocal.remove(e);
    }

    /**
     * Compare les événements de l'agenda local avec les événements chargés par l'ADE.
     */
    public void comparerAgendaEvent(Calendrier calendrier){
        if(agendaLocal != null && calendrier != null)
            calendrier.filtrerDoublons(agendaLocal);
    }

    /**
     * Méthode exportant la liste d'événements vers l'agenda par défaut du téléphone.
     */
    public void exportAgenda(Context context, Calendrier calendrier){
        if(agendaLocal != null && calendrier != null && calendrier.listeEvents != null) {
            for (Event event : calendrier.listeEvents) {
                if(!event.estDoublon()) {
                    ContentResolver cr = context.getContentResolver();
                    ContentValues values = new ContentValues();

                    //On prépare l'événements à enregistrer
                    values.put(CalendarContract.Events.DTSTART, event.getDebut().getTimeInMillis());
                    values.put(CalendarContract.Events.DTEND, event.getFin().getTimeInMillis());
                    values.put(CalendarContract.Events.TITLE, event.getTitre());
                    values.put(CalendarContract.Events.DESCRIPTION, event.getUid() + event.getDescription());
                    values.put(CalendarContract.Events.EVENT_LOCATION, event.getLieu());
                    TimeZone timeZone = event.getDebut().getTimeZone();
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
                    values.put(CalendarContract.Events.CALENDAR_ID, selectedCalendarId);
                    values.put(CalendarContract.Events.HAS_ALARM, event.getAlarme()?1:0);

                    // Enregistre l'événement
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                }
            }
        }
    }

    /**
     * Récupère l'identifiant, ce qui permet ensuite de cibler le bon calendrier sur le téléphone.
     * @return L'identifiant, sous forme de String
     */
    public String getSelectedCalendarId(){return this.selectedCalendarId;}

    /**
     * Charge les ressources à partir du fichier ressources.csv
     * @param context Le contexte appelant la méthode.
     * @return Un String de la forme : "4308" ou "4308,322",...
     */
    public static String chargerRessources(Context context){
        FileInputStream fIn = null;
        InputStreamReader isr = null;

        char[] inputBuffer = new char[255];
        String data = "";

        try{
            fIn = context.openFileInput("ressources.csv");
            isr = new InputStreamReader(fIn);
            isr.read(inputBuffer);
            data = new String(inputBuffer);

            // Parsage empêchant certaines erreurs liées à la lecture bufferisée
            int lastInt = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(
                            data.lastIndexOf('0'),data.lastIndexOf('1')),data.lastIndexOf('2')),data.lastIndexOf('3')),data.lastIndexOf('4')),
                    data.lastIndexOf('5')),data.lastIndexOf('6')),data.lastIndexOf('7')),data.lastIndexOf('8')),data.lastIndexOf('9'));
            data = data.substring(0,lastInt+1);
        }catch (Exception e) {
            Log.e("chargerRessources", "Les ressources n'ont pas pu être chargées");
        }
        return data;
    }

    /**
     * Sauvegarde l'agenda choisi dans un fichier sur le téléphone.
     * <br>Cela permettra au programme de lire ce fichier pour proposer directement cet agenda.
     */
    public static void sauvegarderCalendrier(Context context, String data){
        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;

        try{
            fOut = context.openFileOutput("calendrier.csv",context.MODE_PRIVATE);//MODE_APPEND);
            osw = new OutputStreamWriter(fOut);
            osw.write(data);
            osw.flush();
        }
        catch (Exception e) {
        }
        finally {
            try {
                osw.close();
                fOut.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Charge le dernier agenda utilisé pour enregistrer des événements.
     * @param context Contexte appelant la méthode.
     * @return Un String contenant un chiffre (identifiant de l'agenda) si un enregistrement a déjà été fait, "" sinon.
     */
    public String chargerCalendrier(Context context){
        FileInputStream fIn = null;
        InputStreamReader isr = null;

        char[] inputBuffer = new char[255];
        String data = "";

        try{
            fIn = context.openFileInput("calendrier.csv");
            isr = new InputStreamReader(fIn);
            isr.read(inputBuffer);
            data = new String(inputBuffer);
            int lastInt = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(
                            data.lastIndexOf('0'),data.lastIndexOf('1')),data.lastIndexOf('2')),data.lastIndexOf('3')),data.lastIndexOf('4')),
                    data.lastIndexOf('5')),data.lastIndexOf('6')),data.lastIndexOf('7')),data.lastIndexOf('8')),data.lastIndexOf('9'));
            data = data.substring(0,lastInt+1);
        }catch (Exception e) {
            Log.e("chargerCalendrier", "Le calendrier par défaut n'a pas pu être chargé");
        }
        return data;
    }

    /**
     * Constructeur à utiliser pour la synchronisation uniquement !
     * <br>Il envoit directement la requête pour récupérer le calendrier "favori" sur l'agenda par défaut du téléphone, après vérification des doublons.
     * @param activity Activité appelante ; indispensable pour les "appels système"
     */
    public LocalCal(Context activity) {
        this(activity, ""); //On devra enregistrer le dernier calendrier utilisé, aussi
        Calendrier calendrier = new Calendrier(this.ressource, "4"); //Ressource demandée, 4 semaines
        comparerAgendaEvent(calendrier);
        exportAgenda(activity, calendrier);
    }

    /**
     * Constructeur usuel.
     * @param activity
     * @param selectedCalendarId
     */
    public LocalCal(Context activity, String selectedCalendarId) {
        this.ressource = chargerRessources(activity);
        if(!selectedCalendarId.equals(""))
            //Si on spécifie un identifiant de calendrier, alors on le met
            this.selectedCalendarId = selectedCalendarId;
        else {
            //Sinon on vérifie qu'on en a pas un dans les préférences
            this.selectedCalendarId = chargerCalendrier(activity);
            //Si on avait pas de préférence, alors on prend le default Cal
            if (this.selectedCalendarId.equals(""))
                this.selectedCalendarId = "1";
        }

        Uri l_eventUri;
        agendaLocal = new ArrayList<Event>();

        if (Build.VERSION.SDK_INT >= 8 ) {
            l_eventUri = Uri.parse("content://com.android.calendar/events");
        } else {
            l_eventUri = Uri.parse("content://calendar/events");
        }
        String[] l_projection = new String[]{"title", "dtstart", "dtend"};

       CursorLoader cur = new CursorLoader(activity,l_eventUri, l_projection, "calendar_id=" + this.selectedCalendarId, null, "dtstart ASC, dtend ASC");
        Cursor l_managedCursor = cur.loadInBackground();
        if (l_managedCursor.moveToFirst()){
            String l_title;
            int l_colTitle = l_managedCursor.getColumnIndex(l_projection[0]);
            int l_colBegin = l_managedCursor.getColumnIndex(l_projection[1]);
            int l_colEnd = l_managedCursor.getColumnIndex(l_projection[2]);

            do {
                l_title = l_managedCursor.getString(l_colTitle);
                agendaLocal.add(new Event(l_title,l_managedCursor.getString(l_colBegin),l_managedCursor.getString(l_colEnd)));
            } while (l_managedCursor.moveToNext());
        }
    }

    /**
     * Méthode permettant de ne récupérer que les événements d'une date donnée.
     * @param date Date pour laquelle on veut les événements
     * @return Une ArrayList d'Event de même date (jour/mois/année).
     */
    public ArrayList<Event> listeEventsJour(GregorianCalendar date){
        ArrayList<Event> liste = new ArrayList<Event>();

        for(Event e: this.agendaLocal) {
            if(date.get(Calendar.DAY_OF_YEAR) == e.dateDebut.get(Calendar.DAY_OF_YEAR))
                liste.add(e);
        }
        return liste;
    }
}
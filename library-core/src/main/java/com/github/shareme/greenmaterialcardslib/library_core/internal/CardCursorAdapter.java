/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package com.github.shareme.greenmaterialcardslib.library_core.internal;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.shareme.greenmaterialcardslib.library_core.R;
import com.github.shareme.greenmaterialcardslib.library_core.internal.base.BaseCardCursorAdapter;
import com.github.shareme.greenmaterialcardslib.library_core.view.CardListView;
import com.github.shareme.greenmaterialcardslib.library_core.view.CardView;
import com.github.shareme.greenmaterialcardslib.library_core.view.base.CardViewWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;


/**
 * Cursor Adapter for {@link Card} model
 *
 *
 * </p>
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
@SuppressWarnings("unused")
public abstract class CardCursorAdapter extends BaseCardCursorAdapter {

    protected static String TAG = "CardCursorAdapter";

    /**
     * {@link CardListView}
     */
    protected CardListView mCardListView;

    /**
     * Internal Map with all Cards.
     * It uses the card id value as key.
     */
    protected HashMap<String /* id */,Card>  mInternalObjects;


    /**
     * All ids expanded
     */
    protected final List<String> mExpandedIds;

    /**
     * Recycle
     */
    protected boolean recycle = false;
    // -------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------

    public CardCursorAdapter(Context context) {
        super(context, null, 0);
        mExpandedIds = new ArrayList<String>();
    }

    protected CardCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mExpandedIds = new ArrayList<String>();
    }

    protected CardCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mExpandedIds = new ArrayList<String>();
    }

    // -------------------------------------------------------------
    // Views
    // -------------------------------------------------------------


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Check for recycle
        if (convertView == null) {
            recycle = false;
        } else {
            recycle = true;
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layout = mRowLayoutId;
        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return mInflater.inflate(layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        CardViewWrapper mCardView;
        Card mCard;

        mCard = (Card) getCardFromCursor(cursor);
        if (mCard != null) {
            mCardView = (CardViewWrapper) view.findViewById(R.id.list_cardId);
            if (mCardView != null) {
                //It is important to set recycle value for inner layout elements
                mCardView.setForceReplaceInnerLayout(Card.equalsInnerLayout(mCardView.getCard(),mCard));

                //It is important to set recycle value for performance issue
                mCardView.setRecycle(recycle);

                //Save original swipeable to prevent cardSwipeListener (listView requires another cardSwipeListener)
                boolean origianlSwipeable = mCard.isSwipeable();
                mCard.setSwipeable(false);

                mCard.setExpanded(isExpanded(mCard));

                mCardView.setCard(mCard);

                //Set originalValue
                //mCard.setSwipeable(origianlSwipeable);
                if (origianlSwipeable)
                    Timber.d(TAG, "Swipe action not enabled in this type of view");

                //If card has an expandable button override animation
                if ((mCard.getCardHeader() != null && mCard.getCardHeader().isButtonExpandVisible()) || mCard.getViewToClickToExpand()!=null ){
                    setupExpandCollapseListAnimation(mCardView);
                }

                //Setup swipeable animation
                setupSwipeableAnimation(mCard, mCardView);

                //setupMultiChoice
                setupMultichoice(view,mCard,mCardView,cursor.getPosition());
            }
        }
    }


    /**
     * Sets SwipeAnimation on List
     *
     * @param card {@link Card}
     * @param cardView {@link CardView}
     */
    protected void setupSwipeableAnimation(final Card card, CardViewWrapper cardView) {

        cardView.setOnTouchListener(null);
    }

    /**
     * Overrides the default collapse/expand animation in a List
     *
     * @param cardView {@link CardView}
     */
    protected void setupExpandCollapseListAnimation(CardViewWrapper cardView) {

        if (cardView == null) return;
        cardView.setOnExpandListAnimatorListener(mCardListView);
    }


    // -------------------------------------------------------------
    //  Expanded
    // -------------------------------------------------------------

    /**
     *  Set the card as Expanded.
     */
    public void setExpanded(Card card) {
        if (card!=null)
            setExpanded(card.getId());
    }

    /**
     *  Set the card as Expanded using its id
     */
    public void setExpanded(final String id) {
        if (mExpandedIds!=null){
            if (mExpandedIds.contains(id)) {
                return;
            }
            mExpandedIds.add(id);
        }
    }


    /**
     *  Set the card as collapsed
     */
    public void setCollapsed(Card card) {
        if (card!=null)
            setCollapsed(card.getId());
    }

    /**
     *  Set the card as collapsed using its id
     */
    public void setCollapsed(final String id) {
        if (mExpandedIds!=null){
            if (!mExpandedIds.contains(id)) {
                return;
            }
            mExpandedIds.remove(id);
        }
    }


    /**
     * Indicates if the item at the specified position is expanded.
     *
     * @param card
     * @return true if the view is expanded, false otherwise.
     */
    public boolean isExpanded(Card card) {
        String itemId = card.getId();
        return mExpandedIds.contains(itemId);
    }

    /**
     * Checks if the item is already expanded
     *
     * @param viewCard
     * @return
     */
    public boolean onExpandStart(CardViewWrapper viewCard) {
        Card card = viewCard.getCard();
        if (card!=null){
            String itemId = card.getId();
            if (!mExpandedIds.contains(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * * Checks if the item is already collapsed
     *
     * @param viewCard
     * @return
     */
    public boolean onCollapseStart(CardViewWrapper viewCard) {
        Card card = viewCard.getCard();
        if (card!=null){
            String itemId = card.getId();
            if (mExpandedIds.contains(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the mExpandedIds after an expand action
     *
     * @param viewCard
     */
    public void onExpandEnd(CardViewWrapper viewCard) {
        Card card = viewCard.getCard();
        if (card!=null){
            setExpanded(card);
        }
    }

    /**
     * Updates the mExpandedIds after a collapse action
     *
     * @param viewCard
     */
    public void onCollapseEnd(CardViewWrapper viewCard) {
        Card card = viewCard.getCard();
        if (card!=null){
            setCollapsed(card);
        }
    }

    // -------------------------------------------------------------
    //  Getters and Setters
    // -------------------------------------------------------------

    /**
     * @return {@link CardListView}
     */
    public CardListView getCardListView() {
        return mCardListView;
    }

    /**
     * Sets the {@link CardListView}
     *
     * @param cardListView cardListView
     */
    public void setCardListView(CardListView cardListView) {
        this.mCardListView = cardListView;
    }


    /**
     * Returns the expanded ids
     *
     * @return
     */
    public List<String> getExpandedIds() {
        return mExpandedIds;
    }
}

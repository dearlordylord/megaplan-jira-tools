(function() {
    AJS.MegaMultiSelect = AJS.MultiSelect.extend({

    addItem: function(descriptor, initialize) {

            // this descriptor is for the lozenge so we don't want to use the same descriptor but a copy instead. We don't
            // want our descriptor properties to change through a reference we were unaware of.
            if (descriptor instanceof AJS.ItemDescriptor) {
                descriptor = AJS.copyObject(descriptor.allProperties(), false);
            }



            descriptor.value = AJS.$.trim(descriptor.value);
            descriptor.label = AJS.$.trim(descriptor[this.options.itemAttrDisplayed]) || descriptor.value;
            descriptor.title = AJS.$.trim(descriptor.title) || descriptor.label;

            descriptor = new AJS.ItemDescriptor(descriptor);

            if (this._isItemPresent(descriptor)) {
                return;
            }

            var lozenge = this.options.itemBuilder.call(this, descriptor);

            this.lozengeGroup.addItem(lozenge);
            this._assignEvents("lozenge", lozenge);

            this.model.setSelected(descriptor);

            this.updateItemsIndent();

            this.dropdownController.setPosition(); // update position incase another line has been added

            lozenge.value = descriptor.value(); // we use this to prevent duplicates being added


            var mselect = this
            var selectedOptions = this.model.$element[0].selectedOptions ||
                jQuery(this.model.$element[0]).find('option:selected')
            if (selectedOptions.length > 1) {
                if ("-1" != descriptor.value()) {
                    jQuery(selectedOptions).each(function(n,o){
                        if ("-1" == o.getAttribute('value')) {
                            //mselect.trigger("removeOption", AJS.$.data(o, "descriptor"))
                            var desc = AJS.$.data(o, "descriptor")
                            mselect.removeItem(desc)
                            mselect.$field.focus();
                            for (var item in mselect.lozengeGroup.items) {
                                var it = mselect.lozengeGroup.items[item];
                                if ("-1" == it.value) {
                                    mselect.lozengeGroup.removeItem(it)
                                }
                            }
                        }
                    })
                } else {
                    console.warn(descriptor)
                    jQuery(selectedOptions).each(function(n,o) {
                        if ("-1" != o.getAttribute('value')) {
                            var desc = AJS.$.data(o, "descriptor")
                            mselect.removeItem(desc)
                            for (var item in mselect.lozengeGroup.items) {
                                var it = mselect.lozengeGroup.items[item];
                                if ("-1" != it.value) {
                                    mselect.lozengeGroup.removeItem(it)
                                }
                            }
                        }
                    })
                }
            }




            if (!initialize) {
                this.model.$element.trigger("selected", [descriptor, this]);
            }
        }

    })
    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
        jQuery('div.field-group select[multiple="multiple"][id^="customfield_"]').each(function(c,e) {
            new AJS.MegaMultiSelect({
                element: jQuery(e),
                itemAttrDisplayed: "label",
                errorMessage: AJS.params.multiselectComponentsError
            });

        })
    })
})()
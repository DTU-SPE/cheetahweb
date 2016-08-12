var cheetah = cheetah || {};

/**
 * Hides a bootstrap modal.
 * @param $scope the scope associated with the modal
 * @param id the id of the modal
 * @param data the data produced in the modal
 */
cheetah.hideModal = function ($scope, id, data) {
    $scope.$emit(id + '.hide', data);
    $('#' + id).modal('hide');
};

/**
 * Shows a bootstrap modal.
 * @param $scope the current scope; the scope of the modal to be shown must be nested
 * @param id the id of the modal to be shown
 * @param data the data to be shown in the modal
 */
cheetah.showModal = function ($scope, id, data) {
    $('#' + id).modal('show');
    $scope.$broadcast(id + '.show', data);
};

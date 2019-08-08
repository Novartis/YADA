<template>
  <div id="query-editor-container" class="modal fade" tabindex="-1" data-backdrop="static">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
          <h5 class="modal-title">Edit Query</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
				</div>
				<div class="modal-body">
					<form>
						<div class="form-group">
							<label for="query-name">Qname:</label>
							<div id="query-name-box" class="">
                  <input id="query-name" v-model="qname" class="form-control" type="text">
                  <button class="copy btn" data-clipboard-target="#query-name"
									        id="query-name-copy" type="button">
                  </button>
							</div>
						</div>

						<div id="query-editor-box" class="form-group">
							<div>
								<label for="query-editor">Query:<span class="instruction"> (Press 'F2' to toggle code window)</span></label> <span
									class="glyphicon glyphicon-copy" data-toggle="tooltip"
									id="query-code-copy"></span>
							</div>
              <CodeMirrorWrap/>
						</div>
						<div class="" id="accordion" role="tablist"
							aria-multiselectable="true">
							<div class="card">
								<div class="card-header" id="headingOne"
									data-toggle="collapse"
									data-target="#collapseOne">
                  Comments
								</div>
								<div id="collapseOne" class="collapse show"
									 data-parent="#accordion" aria-labelledby="headingOne">
									<div class="card-body">
										<div id="query-comments-box" class="form-group">
											<textarea class="form-control" id="query-comments" v-model="comments"
												placeholder="Enter comments or documentation here..."></textarea>
										</div>
									</div>
								</div>
							</div>
							<div class="card">
								<div class="card-header" id="headingTwo"
									data-toggle="collapse"
									data-target="#collapseTwo">
                  Security
								</div>
								<div id="collapseTwo" class="collapse"
									data-parent="#accordion" aria-labelledby="headingTwo">
									<div class="card-body container-fluid">
										<div class="row policy-extension-checkboxes">
											<div class="col-md-4">
												<label for="secure-query-ckbx">Mark this query as secure </label><input
													id="secure-query-ckbx" type="checkbox" value="yes" />
											</div>
											<div class="col-md-8">
                        <label for="secure-app-ckbx"></label><input
                          id="secure-app-ckbx" type="checkbox" value="yes" />
                      </div>
										</div>
										<div class="policy-group">
											<div class="row">
												<div class="col-sm-9">
													<div class="form-group">
													  <input type="hidden" class="policy-id"/>
														<label for="policy-plugin">Plugin</label> <input
															class="form-control policy-plugin" type="text"
															placeholder="Enter FQCN (e.g., mypackage.plugins.SecurityPlugin) or class name (e.g., Gatekeeper)..." />
													</div>
												</div>
												<div class="col-sm-3">
													<div class="form-group">
														<label>Actions</label> <select class="form-control policy-action">
															<option value="" disabled selected>Choose an
																action...</option>
															<option value="save">Save changes</option>
															<option value="remove">Remove selected policies</option>
															<option value="add-same">Add another policy (same plugin)</option>
															<option value="add-new">Add another policy (different plugin)</option>
														</select>
													</div>
												</div>
											</div>
											<div class="row">
												<div class="col-sm-12">
													<div class="form-group">
														<div id="arg-string-group">
															<label for="arg-string">Argument String:</label>
															<div class="col-sm-10 arg-string pull-right" style=""></div>
														</div>
													</div>
												</div>
											</div>
											<div class="security-options">
												<div class="row">
													<div class="col-sm-4">
														<div class="form-group">
															<label for="policy-type">Policy Type</label>
															<select
																class="form-control policy-type">
																<option value="" disabled selected>Choose a
																	policy type...</option>
																<option value="U">URL Pattern Matching</option>
																<option value="T">Token Validaton</option>
																<option value="EC">Execution Policy (Columns)</option>
																<option value="EI">Execution Policy (Indices)</option>
																<option value="C">Content Policy</option>
															</select>
														</div>
													</div>
													<div class="col-sm-7">
														<div class="form-group">
															<label for="policy-arg">Argument</label>
															<textarea class="form-control policy-arg"
																placeholder="Enter argument name=value pair..."></textarea>
														</div>
													</div>
													<div class="col-sm-1">
                          <div class="form-group remove-policy-group">
                            <label for="remove-policy">Remove</label>
                            <input class="remove-policy" type="checkbox" value="1">
                          </div>
                        </div>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="card">
								<div class="card-header" id="headingThree"
									data-toggle="collapse"
									data-target="#collapseThree">
                  Default Parameters
								</div>
								<div id="collapseThree" class="collapse"
									 data-parent="#accordion" aria-labelledby="headingThree">
									<div class="card-body">
										<ParamTable :row-data="getParamTableRows"></ParamTable>
									</div>
								</div>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" id="button-delete" class="btn btn-danger">Delete</button>
					<button type="button" id="button-copy" class="btn btn-secondary">Copy</button>
					<button type="button" id="button-rename" class="btn btn-secondary">Rename</button>
					<button type="button" id="button-cancel" class="btn btn-warning" data-dismiss="modal">Cancel</button>
					<button type="button" id="button-save" class="btn btn-primary">Save</button>
				</div>
			</div>
    </div>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import CodeMirrorWrap from './CodeMirrorWrap.vue'
import ParamTable from './ParamTable.vue'
export default {
  components: { CodeMirrorWrap,ParamTable },
  data() {
    return {
      qname: !!this.getQuery ? this.getQuery.QNAME : null,
      comments: !!this.getQuery ? this.getQuery.COMMENTS : null
    }
  },
  computed: mapGetters([ 'getQname','getQuery','getParamTableRows' ]),
  mounted() {
    // show the modal created by this component on mount
    $('#query-editor-container').modal('show')
    this.qname = this.getQname
    this.comments = this.getQuery.COMMENTS
  }
}
</script>
<style>
  .modal-lg {
    max-width: 90% !important;
  }

  .instruction {
    font-family: sans-serif;
    font-weight: 600;
    font-size: 1rem;
    color: rgb(0,0,0,0.2)
  }

  #query-name-copy {
    margin-top: -31px;
    margin-right: 3px;
  }

  #query-name-copy:before {
    background-size: 16px;
  }

  #collapseOne .card-body { padding: 1px; }
  #query-comments-box { margin-bottom: 0px; }
  #query-comments { border: 0px; }


</style>

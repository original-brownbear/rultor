/**
 * Copyright (c) 2009-2016, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.agents.ecs;

import com.amazonaws.services.ecs.model.Container;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Starts Amazon Ecs instance.
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 2.0
 * @todo #1066 Implement com.rultor.agents.ecs.StopsEcs agent. It must
 *  stop Ecs on-demand containers if it was started at StartsEcsContainer agent.
 *  StopsEcs must use instance id from /talk/ecs/[@id] to stop it.
 * @todo #1066 RegistersShell must register SSH params "host", "port",
 *  "login", "key" for ecs on-demand instance, if this one was successfully
 *  started. Successfully start means that these parameters exist in
 *  /talk/ecs
 * @todo #1066 Add new instance creation classes for StartsEcsContainer and
 *  StopsEcs to com.rultor.agents.Agents.StartsEcsContainer must be invoked
 *  before RegistersShell agent. StopsEcs must be invoked after RemovesShell
 *  agent. Ecs containers need to be started in privileged mode to allow a
 *  Docker daemon to work inside of them.
 * @todo #1066 Create a base Ecs Image that provides SSHD and Docker for
 *  StartsEcsContainer.
 *  This image needs to provide sshd, git, a docker daemon and the sudo command.
 *  Docker must work with and without sudo, so must git. The image must create
 *  a keypair on boot, that is then downloaded an used by StartsEcsContainer
 *  and subsequently RegistersShell to shell into the Ecs container.
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "amazon" })
public final class StartsEcsContainer extends AbstractAgent {
    /**
     * AmazonEC2 client provider.
     */
    private final transient Amazon amazon;

    /**
     * Ctor.
     * @param amaz Amazon
     */
    public StartsEcsContainer(final Amazon amaz) {
        super("/talk[daemon and not(shell)]");
        this.amazon = amaz;
    }

    @Override
    //@todo #629 Add Instance params to Directive, for example publicIpAddress
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Container instance = this.amazon.runOnDemand();
        Logger.info(
            this,
            "EC2 instance %s created",
            instance
        );
        return new Directives().xpath("/talk")
            .add("ec2")
            .attr("id", instance.getContainerArn());
    }
}

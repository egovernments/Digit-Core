import React from "react";
import { Outbox } from "./Outbox";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Outbox",
  component: Outbox,
};

export const Default = () => <Outbox />;
export const Fill = () => <Outbox fill="blue" />;
export const Size = () => <Outbox height="50" width="50" />;
export const CustomStyle = () => <Outbox style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Outbox className="custom-class" />;

export const Clickable = () => <Outbox onClick={()=>console.log("clicked")} />;

const Template = (args) => <Outbox {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

import React from "react";
import { DomainDisabled } from "./DomainDisabled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DomainDisabled",
  component: DomainDisabled,
};

export const Default = () => <DomainDisabled />;
export const Fill = () => <DomainDisabled fill="blue" />;
export const Size = () => <DomainDisabled height="50" width="50" />;
export const CustomStyle = () => <DomainDisabled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DomainDisabled className="custom-class" />;

export const Clickable = () => <DomainDisabled onClick={()=>console.log("clicked")} />;

const Template = (args) => <DomainDisabled {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

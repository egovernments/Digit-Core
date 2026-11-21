import React from "react";
import { IntegrationInstructions } from "./IntegrationInstructions";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "IntegrationInstructions",
  component: IntegrationInstructions,
};

export const Default = () => <IntegrationInstructions />;
export const Fill = () => <IntegrationInstructions fill="blue" />;
export const Size = () => <IntegrationInstructions height="50" width="50" />;
export const CustomStyle = () => <IntegrationInstructions style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <IntegrationInstructions className="custom-class" />;

export const Clickable = () => <IntegrationInstructions onClick={()=>console.log("clicked")} />;

const Template = (args) => <IntegrationInstructions {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
